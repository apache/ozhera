## 1、日志agent采集优化

### 1.1 背景

日志 Agent 是一款用于日志采集的工具，基于 Java 8 开发，部署在每个节点上，负责接收并执行下发的采集路径元数据。每台机器可能包含多个日志路径，因此不可避免地需要使用线程池来处理采集任务。然而，线程池的固定性质导致了一些问题。在之前的版本中，采集路径通常需要指定具体的文件名，而用户更倾向于使用通配符方式指定文件名，导致资源问题。最新版本升级到 Java 20，充分利用了 Java 20 的协程新特性，淘汰了传统线程池的管理方式，完全拥抱了协程，不再担心单机上文件数量过多导致线程池不够用的问题。

Agent的架构图

![agent-structure.png](images%2Fagent-structure.png)

### 1.2 优化示例

#### 升级到 Java 20： 

通过升级到 Java 20 版本，可以使用协程（Coroutines）的新特性，从而改善线程池的管理。不再需要显式创建线程池，而是使用 `Executors.newVirtualThreadPerTaskExecutor()` 创建协程池，从而支持更多任务提交而不必担心拒绝问题。

```Java
public static ExecutorService createPool() {
    System.setProperty("jdk.virtualThreadScheduler.parallelism", String.valueOf(Runtime.getRuntime().availableProcessors() + 1));
    return Executors.newVirtualThreadPerTaskExecutor();
}
```

#### 通配符方式的日志采集： 

优化支持通配符方式的日志采集，通过监听文件系统的事件，实现对文件变化的强大监听。当新文件被创建、文件被修改或文件被删除时，通过 epoll 的事件特性来捕捉这些变化，从而实现高效的日志采集。

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

对文件变化我们需要监听，当文件有内容，我们可以打开文件继续读，当一段时间没有文件写入后，我们需要结束它，需要对机器资源合理利用

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

### 1.3 优化结果

通过 K8s 的 DaemonSet 方式部署 Agent，优化前的配置如下

```YAML
resources:
  limits:
    cpu: '6'
    memory: 4Gi
  requests:
    cpu: '1'
    memory: 2Gi
```

而在优化之后的配置如下：

```YAML
resources:
  limits:
    cpu: '4'
    memory: 2Gi
  requests:
    cpu: '1'
    memory: 2Gi
```

这一系列优化操作降低了对 CPU 和内存的占用。在优化之前，线程池最多包含 1024 个线程，且队列长度为 0，这意味着一旦任务数量超过 1024 个，就会导致任务被拒绝。然而，在优化之后，Agent 能够处理无限数量的文件采集任务。

这些优化措施使得 Agent 在资源利用和性能方面取得了明显的改进，同时允许更多的文件采集任务得以执行

### 1.4 总结

![machine_monitor.PNG](images%2Fmachine_monitor.PNG)

![coll_progress.PNG](images%2Fcoll_progress.PNG)

通过上述图表，我们可以清晰地观察到，升级到 Java 20 后，对内存的使用降低了近一半，这是一个显著的改进。同时，通配符采集方式也变得更加友好和可见，使得同时采集多个文件成为可能。这些优化措施使得日志 Agent 更适合高效、弹性地处理大量文件采集任务，同时支持通配符方式的日志路径，从而提高资源利用和性能。

总结而言，通过这些优化，日志 Agent 在 Java 20 的支持下，能够更好地满足日志采集的需求，解决了线程池管理和通配符路径采集的问题，进一步提升了性能和资源利用效率，使其成为更强大的日志采集工具