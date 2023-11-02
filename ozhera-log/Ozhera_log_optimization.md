## 1.Log Agent Optimization

- ### 1.1 Background

-  The Log Agent is a tool for log collection developed in Java 8 and deployed on each node. It is responsible for receiving and executing assigned log collection path metadata. Each machine may contain multiple log paths, necessitating the use of a thread pool to handle collection tasks. However, the fixed nature of thread pools has posed some challenges. In previous versions, log paths often needed to specify specific file names, while users preferred using wildcard patterns to specify file names, leading to resource issues. In the latest version, an upgrade to Java 20 has fully leveraged Java 20's coroutine features, eliminating the traditional thread pool management approach, and wholeheartedly embracing coroutines, no longer worrying about the pool running out of threads due to an excessive number of files on a single machine.

Agent Architecture Diagram

![agent-structure.png](images%2Fagent-structure.png)

### 1.2 Optimization Examples

#### Upgrading to Java 20:

By upgrading to Java 20, you can utilize the new features of coroutines (Coroutines) to improve thread pool management. You no longer need to explicitly create thread pools; instead, you can use `Executors.newVirtualThreadPerTaskExecutor()` to create a coroutine pool, supporting more task submissions without worrying about rejection issues.

```Java
public static ExecutorService createPool() {
    System.setProperty("jdk.virtualThreadScheduler.parallelism", String.valueOf(Runtime.getRuntime().availableProcessors() + 1));
    return Executors.newVirtualThreadPerTaskExecutor();
}
```

#### Wildcard Log Collection:

Optimize support for wildcard-based log collection by listening to file system events to efficiently capture file changes. When new files are created, files are modified, or files are deleted, use epoll's event capabilities to capture these changes and achieve efficient log collection.

```Java
public void reg(String path, Predicate<String> predicate) throws IOException, InterruptedException {
    Path directory = Paths.get(path);
    File f = directory.toFile();

    if (!f.exists()) {
        log.info("create directory:{}", directory);
        Files.createDirectories(directory);
    }

    Arrays.stream(Objects.requireNonNull(f.listFiles())).filter(it -> predicate.test(it.getPath())).forEach(this::initFile);

    WatchService watchService = FileSystems.getDefault().newWatchService();
    directory.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_CREATE);
    while (true) {
        WatchKey key = watchService.take();
        for (WatchEvent<?> event : key.pollEvents()) {
            Path modifiedFile = (Path) event.context();
            String filePath = String.format("%s%s", path, modifiedFile.getFileName().toString());
            if (!predicate.test(filePath) || modifiedFile.getFileName().toString().startsWith(".")) {
                continue;
            }
            log.debug("epoll result,path:{}", event.kind() + filePath);
            HeraFile hfile = fileMap.get(filePath);

            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                if (null == hfile) {
                    hfile = initFile(new File(filePath));
                }
                modify(hfile);
            }

            if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                fileMap.remove(filePath);
                if (null != hfile) {
                    map.remove(hfile.getFileKey());
                    listener.onEvent(FileEvent.builder().type(EventType.delete).fileName(filePath).fileKey(hfile.getFileKey()).build());
                }
            }

            if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                File file = new File(path + "" + modifiedFile.getFileName());
                Object k = FileUtils.fileKey(file);
                if (map.containsKey(k)) {
                    log.info("change name " + map.get(k) + "--->" + file);
                    listener.onEvent(FileEvent.builder().fileKey(k).type(EventType.rename).build());
                } else {
                    listener.onEvent(FileEvent.builder().type(EventType.create).fileName(file.getPath()).build());
                }
                HeraFile hf = HeraFile.builder().file(file).fileKey(k).fileName(filePath).build();
                map.putIfAbsent(k, hf);
                fileMap.put(filePath, hf);
            }
        }
        key.reset();
    }
```

To listen for file changes, we need to capture them. When files have content, we can open them to continue reading. After a period of no file writes, we need to close them to make efficient use of machine resources.

```Java
public HeraFileMonitor(long removeTime) {
    Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
        try {
            List<Pair<String, Object>> remList = Lists.newArrayList();
            long now = System.currentTimeMillis();
            fileMap.values().forEach(it -> {
                if (now - it.getUtime().get() >= removeTime) {
                    remList.add(Pair.of(it.getFileName(), it.getFileKey()));
                }
            });
            remList.forEach(it -> {
                log.info("remove file:{}", it.getKey());
                fileMap.remove(it.getKey());
                map.remove(it.getValue());
            });
        } catch (Throwable ex) {
            log.error(ex.getMessage(), ex);
        }
    }, 5, 10, TimeUnit.SECONDS);
}
```

### 1.3 Optimization Results

By deploying the Agent using a K8s DaemonSet, the configuration before optimization was as follows:

```YAML
resources:
  limits:
    cpu: '6'
    memory: 4Gi
  requests:
    cpu: '1'
    memory: 2Gi
```

After optimization, the configuration is as follows:

```YAML
resources:
  limits:
    cpu: '4'
    memory: 2Gi
  requests:
    cpu: '1'
    memory: 2Gi
```

These optimization measures have reduced CPU and memory usage. Prior to optimization, the thread pool contained a maximum of 1024 threads with a queue length of 0, meaning that once the number of tasks exceeded 1024, it would result in task rejection. However, after optimization, the Agent can handle an unlimited number of file collection tasks.

These optimization measures have significantly improved resource utilization and performance, allowing more file collection tasks to be executed.

1.4 Conclusion

![machine_monitor.PNG](images%2Fmachine_monitor.PNG)

![coll_progress.PNG](images%2Fcoll_progress.PNG)

From the above charts, we can clearly see that upgrading to Java 20 has reduced memory usage by nearly half, which is a significant improvement. Additionally, wildcard collection has become more user-friendly and visible, enabling the collection of multiple files simultaneously. These optimization measures make the Log Agent more suitable for efficiently and elastically handling a large number of file collection tasks, while also supporting wildcard log path specifications, enhancing resource utilization and performance.

In summary, through these optimizations, the Log Agent, with the support of Java 20, better meets the needs of log collection, addressing issues related to thread pool management and wildcard path collection, further enhancing performance and resource utilization efficiency, making it a more powerful log collection tool.

Regenerate
