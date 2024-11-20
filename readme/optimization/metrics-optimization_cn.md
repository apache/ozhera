<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

-->

# Apache OzHera(incubating)指标服务优化

## 一、优化成果

1、trace-etl-server实例数由200降到最低20个，常驻30个。

2、在2C7G的单实例配置下，资源利用率，尤其是CPU使用率提高到160%-180%。

3、服务吞吐量提高到单实例最高6W QPS的trace数据处理。

4、据Prometheus维护人员反馈：

Prometheus vmagent（预聚合服务）：

（1）峰值CPU使用情况，6.8核下降到3.8核

（2）预聚合指标量下降45%

（3）原始指标量下降50%

5、服务资源利用更加合理，CPU资源几乎全部被计算指标逻辑所占用，甚至与thrift序列化与反序列化占用的资源旗鼓相当：

![profiling.png](images%2Fprofiling.png)

## 二、背景

### （一）trace-etl-server服务介绍

trace-etl-server是将trace数据转换为metric的服务，是OzHera指标计算的主要服务。trace-etl-server处理了所有接入OzHera的应用的所有的trace数据。优化前，观测双十一时峰值处理流量在135W QPS。trace-etl-server除了去做trace转指标的工作外，也会将异常、慢查询的traceID数据存入ES等其他操作。

trace-etl-server的IP、端口通过nacos暴露给Prometheus。Prometheus每隔15s，去每个trace-etl-server实例中拉取指标数据。

每次Prometheus拉取指标后，都会将内存指标数据清空。

优化前的trace-etl-server架构图：

![optimization-before.png](images%2Foptimization-before.png)

### （二）存在的问题

#### 1、消费能力不足，服务吞吐量过低。

作为ETL服务，需要处理大量数据。优化前的trace-etl-server单实例最高只能处理5000+ QPS的数据。大促期间出现消息堆积数量几千万条，严重影响指标的实时性、准确性。

#### 2、服务所占资源过多。

由于低吞吐量，如果想要满足大促的要求，就必须扩容来达到要求。优化前，trace-etl-server大促期间实例数达到240个，非大促实例数有200个。

#### 3、服务资源浪费严重。

虽然部署了这么多的实例，但是单实例的CPU使用率仅为10%，有90%的CPU资源处于空闲状态，优化前的单实例配置为2C4G。

#### 4、服务存在内存溢出的问题。

因为没有有效的内存管理（内存指标超过阈值就清理的逻辑并没有上线），导致大促期间会出现OOM。

#### 5、对于Prometheus来说，指标合并计算压力过大。

优化前，所有的应用的trace数据都是随机分配给200个实例的，这就是说，一个应用的指标，会出现在这200个实例中。由于这些数据点在Prometheus中是分散存储的（指标内部有instance标签，就是trace-etl-server的实例IP加端口），导致查询时会做大量的合并计算，查询速度异常缓慢。虽然使用了VictoriaMetrics的预聚合指标能力来提高查询速度，但是预聚合的能力也是会占用一定资源。

## 三、优化目标

（一）、缩减服务实例数，预期目标是由非大促期间200个实例降低到30个。

（二）、提高服务吞吐量，以达到优化目标1中的要求。

（三）、提高服务资源利用率，尤其是CPU使用率。

（四）、杜绝OOM。

## 四、优化方案

### （一）指标计算移植到探针内

去除中心化指标计算服务（trace-etl-server），在探针（opentelemetry-java-instrumentation）内部实现指标计算逻辑。这样每个业务应用的每个实例，都会暴露自己的业务指标。

#### 1、优点

（1）解决trace-etl-server中心化指标计算的资源占用、资源浪费、吞吐量过低等问题。

（2）解决Prometheus需要聚合指标的问题。

（3）指标服务不用使用拉-删模式，图表promQL的sum_over_time计算可以省略，提高图表展示的速度。

#### 2、缺点

（1）多语言适配困难。指标计算的逻辑如果放在业务侧，需要进行多语言适配。

（2）对与业务影响较大。业务侧存储、暴露的指标数据增大，对于探针内指标计算的代码质量要求极高，同时风险较大。

（3）变更速度慢，变更需要业务应用发版以更新探针。相应的，如果出现问题不能第一时间、全量地进行回滚，需要让业务应用发版处理。

### （二）优化trace-etl-server

对于trace-etl-server的优化，主要体现在缩小锁阻塞的时间上，减小锁粒度，提高CPU使用率，提高消费速率，提高单实例吞吐量，从而降低服务实例数量。同时控制内存中指标的数量，防止指标过多导致的内存溢出。
同时，需要考虑减少Prometheus的指标聚合计算的工作量。

#### 1、优点

（1）依然保留现有架构，对于业务侧无侵入、无感知。

（2）多语言适配能力强。

（3）变更速度快，只需要对trace-etl-server进行发布即可。

#### 2、缺点

（1）需要进行代码优化与重构，对于并发处理的代码要求高。

**基于开源多语言适配性要求，最终选择优化trace-etl-server的方案。**

## 五、优化过程

### （一）查找病因

#### 1、为什么消费速率慢，吞吐量低、占用实例过多

（1）锁阻塞的时间较长

为了保证Prometheus拉取指标的准确性，Prometheus拉取指标的时候和trace-etl-server消费数据的时候，都会竞争同一把锁。因为优化前消费数据时是单线程消费，且锁可重入，所以性能瓶颈会出现在Prometheus拉取指标时，对于消费线程的阻塞。

通过这个思路，打印了Prometheus拉取指标的耗时，发现平时的耗时在500ms以内，大促会上升到2s左右。为什么耗时这么长？在Prometheus拉取指标的方法内部，会去遍历内存中所有的指标，将它们转换为Prometheus的指标数据格式，返回给Prometheus。随着内存指标的增多，这个遍历次数会从几万、几十万，上升到几百万的量级。通过使用Arthas的trace指令，我们发现随着内存指标的增多，在Prometheus拉取指标的方法内部，一个方法的被执行次数达到几百万次，耗时2s由此而来。

![arthas-trace.png](images%2Farthas-trace.png)

虽然Prometheus的请求是15s发送一次，但是试想一下，大促期间每隔15s，消费线程就被阻塞2s，这肯定会拉低整体的消费速率与吞吐量。

#### 2、为什么资源利用率，尤其是CPU占用率很低

（1）消费模型的选择

首先，要说一下trace-etl-server的消费，内部使用的消息队列，是Xiaomi内部自研的名叫Talos（开源为RocketMQ）的消息队列。它类似于Kafka，存在partition概念，partition与consumer是一对一，或者是多对一的关系。之前为了避免并发安全问题，选择了partition与consumer实例一对一的消费模型。这就导致一个trace-etl-server实例，最多只能消费一个partition的数据。
又由于锁阻塞时间过长，导致服务吞吐量低的原因，导致一个trace-etl-server没有能力消费多个partition的数据。

#### 3、为什么存在内存溢出

（1）内存中指标数量过多

优化前，并没有对应用的trace数据进行过滤、分类，使相同应用的trace数据被一个或几个partition实例消费。这就导致，所有trace-etl-server实例，理论上都能消费到所有应用的trace数据，这样再转换为指标，就是所有trace-etl-server实例上，都有一份全量应用的指标数据。这就导致内存中指标数量的高居不下。

#### 4、为什么对于Prometheus来说，指标合并聚合的压力过大

（1）指标数据分散

优化前，一个应用的指标数据分散在不同的trace-etl-server（理论上、极端情况下是所有的trace-etl-server）中。Prometheus拉取指标时，会将指标带上instance标签，也就是trace-etl-server的IP加端口，这就导致虽然是相同的指标，指标里有相同的label，在Prometheus看来，这也是不同的。OzHera目前几乎所有业务指标都是需要按照应用进行聚合，这样Grafana在查询Prometheus时，其实Prometheus需要做大量的聚合操作。这不仅对于Prometheus来说压力倍增，对于OzHera的用户来说，查询的速度（尤其是P99图表）也是相当缓慢。

### （二）优化步骤

#### 1、优化锁

我们已经知道是因为Prometheus拉取耗时过长，导致阻塞消费线程的时间也过长，那解决思路就是降低锁的阻塞时间。这里使用了两个Metrics对象来存储内存指标，每隔15s的时间，会将当前记录指标的对象进行更换，这样锁只用加在更换对象的过程，也就是变量更换指针的过程，这样锁的阻塞时间是很低的，可以忽略不计。

首先是将之前单例的Metrics类改造为支持多例，并且每个实例内部的Collector（真正用于存储指标数据的类）也是单独创建的。

指标对象变更的代码如下：
```java

/**
*
* 每隔15s进行Metrics对象的更换
*/
Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
    // 在prometheus拉取数据之后，才开始启动缓存。防止在服务启动时，
    // prometheus发现较慢，导致cacheData中数据过多，指标会被clear的风险。
    if (startCache) {
        try {
            Stopwatch sw = Stopwatch.createStarted();
            // 加锁
            enterManager.getMonitor().enter();
            log.info("begin change");

            try {
                // 这里需要等待目前正在进行指标转换的请求处理完成，否则可能会出现指标的漏记、丢失
                while (enterManager.getProcessNum().get() > 0) {
                    TimeUnit.MILLISECONDS.sleep(200);
                }
                // 进行Metric对象的更换逻辑
                call.change();
            } catch (Throwable ex) {
                log.error(ex.getMessage(), ex);
            } finally {
                // 释放锁
                enterManager.getMonitor().leave();
                log.info("change use time:{}ms", sw.elapsed(TimeUnit.MILLISECONDS));
            }
            // 将Metric对象中的数据进行异步转换为Prometheus拉取的数据格式，转换为byte[]，
            // 存入缓存中，Prometheus拉取时直接从缓存中读取数据，降低了Prometheus的拉取时间。
            cacheData();
        } catch (Throwable t) {
            log.error("schedule cache data error : ", t);
        }
    }
}, 0, 15, TimeUnit.SECONDS);

```
消费线程的代码：
```java

    @Override
    public void process(List<MessageAndOffset> messages, MessageCheckpointer messageCheckpointer) {
        // 这里巧妙地加锁立马释放锁，达到消费线程之间不进行阻塞的目的。当指标对象开始变更时，又起到阻塞消费线程的作用
        enterManager.enter();
    
        // 这里使用计数器来表示当前有消费线程正在进行指标计算，不能进行指标对象的变更
        enterManager.getProcessNum().incrementAndGet();
        try{
            
            // 指标计算逻辑....
        
        } catch (Throwable throwable) {
            
            log.error("process error, ", throwable);
            
        }finally {
            
            // 这里表示消费线程指标计算已完成，可以进行指标对象的变更
            enterManager.getProcessNum().decrementAndGet();
        }
    }
```

#### 2、控制内存中指标数量

通过控制cacheData中的数量，防止Prometheus长时间没有拉取，导致内存中指标数量过多。

#### 3、第一次发布

通过对于锁粒度的控制、锁阻塞时间的缩减，第一次发布线上后，能够将trace-etl-server的实例数量由200个下降到100个。当下降到80个时，就开始报OOM。通过分析发现目前每个实例中基本上保存的是所有应用的指标数据，这个数据量是非常大的。

#### 4、将trace消息按应用名进行分类

按照之前的分析，需要对应用进行分类，即应用的trace数据按照应用名进行hash，使得相同的应用的trace数据只能散布在两个partition上，这样就会大大缩减每个trace-etl-server实例的指标数量。按照这个想法，改造log-agent，利用Talos partitionKey的特性（开源使用RocketMQ的MessageQueue），构造Talos Message对象时，将partitionKey设置为应用名称，并随机在应用名称前后添加字符串。其实Talos内部也会使用partitionKey，按照当前topic partition数量进行hash，得出当前消息需要被发送到哪个partition去处理。

#### 5、第二次发布

这次改动发布之后，200个实例的时候就已经出现了个别partition消息堆积的问题。

#### 6、发现热点应用

trace-etl-server上暴露了一个指标，这个指标记录了当前实例处理了多少条数据，可以按业务应用去分组统计。首先，在Talos（开源为RocketMQ）监控上查看有消息堆积的partition对应的trace-etl-server都是哪些。然后，通过在Prometheus后台，去查询这些trace-etl-server实例的指标中的应用分布，发现这些实例无独有偶，都有一些“热点应用”的数据量非常高。这也就说明了，一些trace数据量非常高的应用，被分配到了这些trace-etl-server实例中，导致trace-etl-server消费不过来，产生消息堆积。

![prometheus-background.png](images%2Fprometheus-background.png)

但是，我们同时也发现这些产生消息堆积的trace-etl-server实例中，这些实例的CPU都没有被打满，说明他们并没有达到性能瓶颈。

#### 7、调整Talos参数，发现Talos成为瓶颈

发现CPU没有被打满后，想通过调整Talos的消费端的参数，提高单批消息的拉取量，降低批次之间的拉取间隔，来提高trace-etl-server的消费能力。但是，我们发现，Talos在消费端，对于这些参数都有限制。每批消息数量最大是5000条，批次之间的拉取间隔最低是50ms。

从这个角度看，每个Talos consumer实例最大消息处理速率应该是10W QPS。但是实际上由于各种损耗，消费速率是达不到这个值的。

#### 8、调整热点应用

Talos调整参数走不通，只能是把这些热点应用的trace数据通过配置的方式，hash到更多的partition上，这就需要改造log-agent，能够配置这些热点应用的partition数量。

#### 9、第三次发布

通过调整同步应用的partition数量，我们将trace-etl-server实例数成功下降到50个。同时，我们很好奇为什么这些应用会产生这么多的trace数据？

#### 10、排查热点应用产生的原因

首先，存储在ES中的trace数据，是会记录这条span的操作名（operationName）是什么，是由哪个框架产生的。然后，我们利用Kibina的数据统计分析，找到这些热点应用的操作占比，发现都是Redis的命令，有一些应用的Redis的span占到这个应用所有span总数的90%。

![ES-visualize.png](images%2FES-visualize.png)

通过查看其中的一些应用使用Redis的代码，以及检查Lettuce、Jedis在opentelemetry-java-instrumentation中的拦截代码，发现并没有异常。

在lettuce 5.1版本之后，它提供了trace扩展，在opentelemetry-java-instrumentation中，实现了lettuce的Tracing接口。

对于Jedis，opentelemetry-java-instrumentation直接拦截了sendCommand方法。

猜测应该是业务侧在Redis集群模式下，所有的批量操作其实都是单条去发送的。

#### 11、探针中过滤部分Redis Command请求

我们查看了包含此类Redis请求的链路，发现他们的链路的节点基本上长的没法看，比如说：

![trace-too-lang.png](images%2Ftrace-too-lang.png)

满屏的Redis操作，根本看不到异常与满查询节点。所以，我们认为这种Redis过多的节点，并且没有异常与慢查询出现，对于业务排查问题来说是灾难。

于是，我们改造了opentelemetry-java-instrumentation中的Lettuce与Jedis的instrumentation，将一些热点应用中占用过多的Redis进行过滤，如果这个节点不是异常，我们将不再生成span。

#### 12、使用JDK20

JDK20中的协程相较于Java线程来说更加轻量，对于I\O密集型的服务来说，使用协程基本能够使服务吞吐量不受线程限制。ZGC能够提高GC效率，降低STW耗时。

## 六、继续优化的建议

1、由于trace-etl-server目前是多线程消费，可以使用读写锁代替ReentrantLock，降低消费线程之间的锁竞争消耗。

2、目前接收Prometheus拉取的HttpServer，使用的是JDK自带的HttpServer，可以改为使用SpringBoot提供Http服务，使用内置的Tomcat或Jetty，更加安全稳定。

3、在更换Metric对象时，如果发现当前有线程正在进行指标计算，则会等待200ms，等指标计算线程处理完毕。这里200ms可以适当降低。

4、trace-etl整个服务的抽象、重构。目前的方向是使用extension，达到对于扩展开放，对于修改关闭。