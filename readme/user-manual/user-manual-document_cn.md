# OzHera用户手册

## 一、应用中心

### （一）应用看板

#### 1、应用搜索

添加到我关注的项目或我参与的项目（如下图）

![ozhera-home-page.jpg](images/ozhera-home-page.jpg)

注：有参与标记的是在对应平台有项目权限的项目，可以添加到我参与的项目列表。否则可以添加到关注项目列表。

#### 2、应用观测

可以在应用中心看到我参与的应用和我关注的应用。

![ozhera-app-list.jpeg](images%2Fozhera-app-list.jpeg)

每个项目卡片，能看到每个项目的告警，调用异常，慢查询等指标的统计（点击进入详情，后面教程会有详情页的说明）。

## 二、指标监控

### （一）指标监控

### 1、Dashboard

#### 1）基础监控

在dashboard中，主体上分为基础监控看图和自定义看图，基础监控包含应用健康度看图、业务指标看图、中间件看图、JVM看图等。

##### (1) 应用健康度

选择好自己的应用后（可以进一步选择环境、ip、时间范围），可以看到可用性、实例列表、调用量三个看图，其中可以主机列表和容器列表可以跳转到该物理机和容器的更加详细的监控，如下图所示：

![ozhera-app-healthy.png](images%2Fozhera-app-healthy.png)

![ozhera-py-machine.png](images%2Fozhera-py-machine.png)

![ozhera-container.png](images%2Fozhera-container.png)

##### (2) 业务指标

业务指标提供http、dubbo调出（自己作为consumer）、dubbo调入(自己作为provider)三种调用方式的qps、p99-rt、avg-rt、top10-rt的图表。

![ozhera-business-metrics.png](images%2Fozhera-business-metrics.png)

##### (3) 中间件

中间件看图目前提供db及redis的p99-rt、avg-rt、top10-rt、top10-query等，如下图所示：

![ozhera-middleware.png](images%2Fozhera-middleware.png)

##### (4) JVM

jvm监控看图提供多维度的指标供用户查看，如下图所示：

![ozhera-jvm.png](images%2Fozhera-jvm.png)

#### 2）自定义看图

自定义看图，是提供给业务侧自己上报prometheus数据并自己配置看图的部分。需要用户熟悉promql，及grafana模版配置。

关于自定义看图更加详细的描述及使用方式参考：[OzHera自定义打点监控文档](..%2Fcustom-tracking%2Fcustom-tracking_cn.md)

### 2、调用异常概况

#### 1）异常列表

![ozhera-exception-list.png](images%2Fozhera-exception-list.png)

注：调用异常概况包括 http调用异常列表，dubbo调用异常列表，db调用异常列表（上图举例http）。

#### 2）异常详情

![ozhera-exception-detail.png](images%2Fozhera-exception-detail.png)

注：上图点击详情跳转页，包括异常基本信息，**调用链路**和**日志**。

![ozhera-trace1.png](images%2Fozhera-trace1.png)

### 3、慢查询概况

#### 1）慢查询列表

![ozhera-slow-list.png](images%2Fozhera-slow-list.png)

#### 2）慢查询详情

![ozhera-slow-detail.png](images%2Fozhera-slow-detail.png)

点击详情如上图，能看到慢调用信息和**调用链路**。

### 4、接口大盘

hera监控中，点击接口大盘，有如下图的监控看图，分为Dubbo Provider接口总览、Dubbo Provider接口大盘、Dubbo Consumer接口总览、Dubbo Consumer接口大盘、Http Server接口总览 以及Http Server接口大盘。

接口总览后缀的监控图表是以Table形式组织的监控数据，分为所有service聚合的总览、Dubbo service级别的总览和Dubbo method级别的总览。

![ozhera-large-cap.png](images%2Fozhera-large-cap.png)

接口大盘后缀的监控图表是看图的形式组织的信息，可以选择多个Dubbo的service进行查看。

![ozhera-interface-large-cap.png](images%2Fozhera-interface-large-cap.png)

### 5、项目大盘列表

项目大盘功能设计的初衷是满足用户查看多个服务聚合的重要信息的需求而开发的，例如在小组leader可能关心小组内所有服务整体的一个稳定性，或者大促时，将一些重要服务添加到项目大盘里，重点看这个图表即可等类似的需求。

如下图所示，可以添加若干服务到包含应用中。

![ozhera-create-large-cap.png](images%2Fozhera-create-large-cap.png)

创建好的大盘会显示在列表中，点击操作中的大盘按钮即可跳转到大盘监控

![ozhera-large-cap-list.png](images%2Fozhera-large-cap-list.png)

在大盘中，可以查看多个服务的DB、http、dubbo、redis、load等重要的信息，并且在下方的应用列表中，点击应用的名字，可以快速跳转到此服务的详细监控页面。

![ozhera-large-cap-detail.png](images%2Fozhera-large-cap-detail.png)

### 6、资源利用率大盘列表

资源利用率大盘，可以快速查看到一个服务部署信息的相关监控图表，区别于容器监控图表的地方是，该图表是能够对一个服务的多个实例聚合查看监控信息的图表，方便一览该服务总体部署资源的情况，如下图所示：

![ozhera-resource-used.png](images%2Fozhera-resource-used.png)

### （二）报警设置

### 1、应用报警策略

**前言**：

应用报警策略是为了更好的将报警进行分类而设计的，有了报警策略我们可以更好的划分报警规则，将同类的、类似的报警规则放到一个报警策略里，这样维护起来比较方便，可以提高操作效率。

**名词解释**：

报警规则：指的是一条报警配置。

报警策略：对应一组报警规则。可以更好的将报警规则进行分类管理、维护。

**对应关系**：

一个应用可以配置多个报警策略、一个报警策略下可以配置多条报警规则。

#### 1）告警策略列表

首先点击左侧的菜单应用报警策略，右侧会打开报警策略列表页面。报警策略列表页面可以按照应用名称、策略名称、监控类型（基础指标、接口指标、自定义promql）等维度对报警策略进行筛选查询。

在报警策略列表页面上，可以起停报警策略、复制报警策略、编辑报警策略、删除报警策略。

![ozhera-alert-strategy.png](images%2Fozhera-alert-strategy.png)

注：项目参与者可以搜索，添加，复制，修改，删除规则，**关注者仅能查看规则，不能复制，修改和删除**

#### 2）告警策略创建

点击报警策略列表的左上角的新建按钮，则进入创建报警策略页面，可以进行报警策略相关信息填写。

首先选择需要创建报警策略的应用，然后填写必填项：

策略名称、报警规则（指标、操作服、阈值、持续时间、报警级别、报警频率）、通知人、@人员列表。填写完毕后，点击下面的确定按钮，完成报警策略的创建。

![ozhera-alert-strategy-create.png](images%2Fozhera-alert-strategy-create.png)

注：选择的应用需有权限（我参与的应用列表）的项目，才可以创建策略。

#### 3）报警策略分类

报警策略整体分为三类：

    基础类指标报警：硬件相关的指标监控；
    
    接口类指标报警：业务相关的接口指标监控；
    
    自定义指标报警：用户自定义的promQl报警配置；

**注意**：三类报警规则，不可在同一个报警策略中混合搭配。一个报警策略只能配置一种类型的报警规则。

##### (1) 基础类监控

基础类报警策略，指的是应用的运行的硬件环境相关的指标，如cpu、内存、负载、jvm相关的指标：

    k8s容器cpu使用率：最近1分钟cpu使用率(平均值) > 阈值
    
    k8s容器机负载：最近1分钟负载(平均值) > 阈值
    
    k8s容器内存使用率：最近1分钟内存平均使用率 > 阈值
    
    k8s容器数量：最近5分钟内上报过状态的容器数量 < 阈值(可以用来监控容器宕机报警)
    
    k8s容器cpu资源利用率（1d）：时常一天（24h）的cpu资源利用率，可用于观测资源的使用效率是否过低
    
    k8s容器内存资源利用率（1d）：时常一天（24h）的内存资源利用率，可用于观测内存资源的使用效率是否过低
    
    HeapUsed：堆内存使用率，最近1分钟内JVM堆内存平均使用率 > 阈值
    
    NO-HeapUsed：最近1分钟JMV非堆内存平均使用率 > 阈值
    
    线程数量：最近1分钟内线程数量 > 阈值
    
    GC次数：最近1分钟内发生GC次数 > 阈值
    
    GC耗时：最近1分钟内某次GC耗时 > 阈值
    
    FullGC次数：最近1分钟内发生FullGC次数 > 阈值
    
    FullGC耗时：最近1分钟内某次FullGC耗时 > 阈值

##### (2) 接口类监控

接口类报警策略，主要是和业务相关的指标报警，比如http、httpClient、dubbo、db（目前只有mysql）等报警配置。

**http**：

    http/httpClient异常数：最近30秒，某个接口调用异常数量（httpCode!=200的情况）
    
    http/httpClient可用性：最近30秒，某个接口调用成功数量除以接口调用总数量（30秒可用率）
    
    http_qps/httpClient_qps：最近秒，某个接口调用总次数除以30秒（30秒平均qps）
    
    http/httpClient平均响应时间：最近30秒，某个接口总耗时时间除以总调用次数（30秒平均响应时间）

**dubbo**

    dubboProvider/dubboConsumer异常数：最近30秒，某个接口调用异常数量（接口抛出异常）
    
    dubboProvider/dubboConsumer可用性(provider)：最近30秒,某个接口调用成功数量除以接口调用总数量（30秒可用率）
    
    dubboProvider/dubboConsumer_qps：最近30秒，某个接口调用总次数除以30秒（30秒平均qps）
    
    dubboProvider/dubboConsumer平均响应时间：最近30秒，某个接口总耗时时间除以总调用次数（30秒平均响应时间）
    
    dubbo慢查询：最近30秒，某个接口慢调用数量（耗时大于1秒，后面考虑可配置）

**db**

    db异常数：最近30秒，某个接口调用异常数量（db调用异常）
    
    db可用性：最近30秒，某个接口调用成功数量除以接口调用总数量（30秒可用率） 
    
    db慢查询：最近30秒，某个接口慢调用数量（耗时大于1秒，后面靠可配置）

##### (3) 自定义promQL

对于自己自定义打点的指标，需要自己手写promql配置报警，配置地点：

![ozhera-customize-promql.png](images%2Fozhera-customize-promql.png)

填写以后，可以点击右下角的链接测试自己的promql：

![ozhera-customize-promql-test.png](images%2Fozhera-customize-promql-test.png)

## 三、链路追踪

### （一）链路追踪查询界面

### 1、首页图表

![ozhera-trace2.jpeg](images%2Fozhera-trace2.jpeg)

#### 1）菜单栏

用于切换trace列表查询、traceID详情查询页面。

#### 2）顶部工具栏

可以输入应用名查询。点击”高级筛选“，可以打开多条件查询框：

![ozhera-trace3.png](images%2Fozhera-trace3.png)

    可以按照开始时间、结束时间查询。
    
    可以按照Tag查询。
    
    可以按照应用操作（接口名称、方法名、db语句等）查询。
    
    可以按照tag查询，例如：http.status_code=500，表示http返回status为500的trace；error=true，表示trace中包含error信息的......
    
    可以按照持续时间范围查询。

#### 3）耗时分布图

可以直观的看到当前查询的trace的耗时分布，包含错误的trace会以亮眼的红色标识。

#### 4）trace列表

    ”状态“，展示了当前trace是否包含错误，如果包含，会以红色展示
    
    ”traceID“，点击可以跳转至trace详情页面
    
    ”执行操作“，展示了根节点span的接口名称、方法名或者db语句等
    
    ”开始时间“，展示了当前trace的开始时间
    
    ”持续时间“，展示了当前trace的耗时
    
    ”Span“，展示了当前trace总共有多少个span

### 2、trace详情

![ozhera-trace-detail.jpeg](images%2Fozhera-trace-detail.jpeg)

#### 1）traceID搜索框

可以按照traceID搜索

#### 2）trace节点看板

展示了应用名、状态、方法、耗时、当前节点实例IP等信息

#### 3）节点信息

![ozhera-trace-tag.png](images%2Fozhera-trace-tag.png)

    Tags：展示了当前节点的标签，比如http状态、rpc信息、db信息等
    
    Process：展示了当前节点IP、hostname、应用名等信息
    
    Log：展示了探针在当前节点记录的信息，比如error stack log、业务状态码、dubbo出入参数据等
    
    Warnings：展示了当前节点在trace链路上的警告信息，比如服务器时钟不一致导致的时间偏差、父节点数据丢失导致的节点信息不全等

### （二）链路追踪的额外能力

### 1、自定义trace（方法级别）

1、添加了javaagent探针

2、引入pom：

  ```xml
      <dependency>
        <groupId>run.mone</groupId>
        <artifactId>hera-trace</artifactId>
        <version>1.4.0</version>
      </dependency>
  ```

3、在需要自定义trace的方法上添加com.xiaomi.hera.trace.annotation包的@Trace注解，eg：

![ozhera-trace-customize.png](images%2Fozhera-trace-customize.png)

### 2、HeraContext——自定义链路上下文

有很多时候，我们希望能在全链路上传递一些信息，类似于traceID能在全链路串联一样的能力。HeraContext开放了类似于传递traceID的能力，允许在链路中的一个节点设置一些信息，这些信息会随着调用链路一直向下传递。
使用方法：

1、添加了javaagent探针

2、引入pom

  ```xml
      <dependency>
        <groupId>run.mone</groupId>
        <artifactId>hera-trace</artifactId>
        <version>1.4.0</version>
      </dependency>
  ```

3、java代码中使用HeraContextUtil类中提供的getContext、get、set方法进行存取。

    getContext()方法，返回的Map中key固定为heracontext，值为当前链路中且是当前节点下存储的context内容。
    get(String key)方法，根据Context key获取value。
    set(String key， String value)方法，将key\value存入当前链路Context中，并向下传递。

4、前端传递Context
Http request header中添加key为heracontext，值为key1:value1;key2:value2;....格式的字符串。 eg：

![ozhera-trace-heracontext.png](images%2Fozhera-trace-heracontext.png)

5、HeraContext使用限制

（1）为了下游服务性能考虑，在一条链路中，HeraContext最多只能存储8个键值对，如果当前节点HeraContext中的键值对已经有8个，那么set方法将无法存储。

### 3、获取traceID与spanID

1、添加了javaagent探针

2、引入pom

  ```xml
      <dependency>
        <groupId>run.mone</groupId>
        <artifactId>hera-trace</artifactId>
        <version>1.4.0</version>
      </dependency>
  ```

3、java代码中使用TraceIdUtil类中提供的traceId、spanId方法进行获取。

## 四、日志服务

### （一）相关概念

### 1、Space

空间（Space）是日志服务中的资源管理单元，用于资源隔离和控制。

#### 1）创建

可在日志服务->日志接入处进行创建：

![ozhera-space1.png](images%2Fozhera-space1.png)

![ozhera-space2.png](images%2Fozhera-space2.png)

#### 1）授权

Space 创建后，默认仅自己可见，可在日志服务->日志接入->Space列表处点击授权，去 Mi-TPC 系统给其他用户授权：

![ozhera-log-space-list.png](images%2Fozhera-log-space-list.png)

权限说明：

1. 自己创建的Space理论上只有自己可见

2. 如果我是管理员，即我是TPC中logger的节点的管理员或者是logger父节点的管理员，就可以看到其他人创建的Space

3. 我自己创建的Space，可以到TPC中这个space名称节点的成员tab中添加成员，之后其他人也就可以看到

### 2、Logstore

Logstore 是同一种日志类型的集合，不支持混搭。

对于 Hera log 而言：
1. Logstore 是日志数据的采集、存储和查询单元
2. 一个 Logstore 中包含的是同一种日志类型的集合，Logstore 中配置的字段集合必须是其下各 Logtail 配置的日志字段的并集

### 3、Logtail

Logtail 是日志实例。

对于 Hera log 而言：
1. Logtail 对应每一个接入的实例的每个环境的日志的集合
2. 一个 Logstore 下的不同 Logtail 的配置中各个日志字段表示的含义需相同，顺序可以不同

### （一）接入与使用

### 1、接入应用到Hera平台

首先需要进入TPC平台的hera节点中新增节点：

    节点类型：项目，
    节点名称即为：项目名称，
    外部节点类型：项目，
    外部节点ID: 以后的数据传递这个值可以作为唯一ID使用，如果没有，推荐不用填
    描述：简单说明

![ozhera-log-tpc.png](images%2Fozhera-log-tpc.png)

![ozhera-log-tpc-add.png](images%2Fozhera-log-tpc-add.png)

### 2、创建logstore

输入 logstore 名称，选择机房和日志类型：

![ozhera-log-store-create.png](images%2Fozhera-log-store-create.png)

**配置详细含义：**

- 机房：目前支持大陆机房
- 日志类型
    - 多行：多行应用日志、opentelemetry 日志
    - 单行：单行应用日志、nginx 日志、docker 日志、自定义日志
      注：多行应用日志，若在创建 logtail 时不指定行首正则，则默认日志必须以时间开始，例如20...或者[20...，否则多行采集会不正常。
- 存储生命周期：ES 索引中数据保存时长
    - 目前，ES中索引的生命周期，创建索引的时候指定，默认是保留5天
- 存储分片数：目前该配置未用到，默认即可
- mq 资源列表及 es 列表：（只有当用户为非管理员时可看到，怎么样成为管理员参考上边）
    - 由于进行资源隔离，非管理员用户需要先去资源管理页面添加rocketMq和ES资源信息，详情请参考资源管理说明
    - 如果资源初始化完成，会出现 mq 资源列表和 es 列表，选择你初始化的 mq 信息和 es 信息即可接入完成
    - 如果是管理员，则不会看到这2项，默认会自动选择管理员创建的这2项资源信息

### 3、创建logtail

![ozhera-log-tail-create.png](images%2Fozhera-log-tail-create.png)

**配置含义：**

- 应用类型：hera
  - 选择不同类型后，会出现一些针对特定类型的选项，根据实际情况填写或选择即可。
- 日志文件路径：
  - 具体到文件名，如：/home/work/log/xxx/server.log。
  - 支持模糊，如：/home/work/log/*.log。
- 切分表达式：日志文件切分的命名规则。
  如果日志切分后和当前目录不在一起，且切分后的文件名的前缀不是日志为名的，则要自己手动填写切分表达式，例如：/home/work/logs/applogs/old/shopapi_application.log-.*。否则可不填。
- 行首正则表达式：如：^2022-* .*$。
- 日志解析类型：分割符、自定义、正则、json、nginx。
- 解析脚本：
  - 分割符：每一段是怎么分割的，输入分割你日志的分割符。
  - 自定义：如果是比较复杂的，可能每一部分都分割都不一样，则用自定义，例如：[%s]-[%s]-[%s]-[%s]-[%s]-%s，会根据 %s 查找每一部分，每一部分的前缀和后缀
  - 正则：输入正则表达式，会根据捕获组来提取字段。
  - nginx：输入 nginx 日志格式配置，例如：

  ```
    log_format milog2  
      '$time_iso8601\t$msec\t$host\t$request_method\t$scheme\t$status\t'
      '$upstream_addr\t$upstream_cache_status\t$upstream_status\t'
      '$server_addr\t$remote_addr\t$http_x_forwarded_for\t'
      '$request_length\t$request_time\t$bytes_sent\t$upstream_response_time\t'
      '$upstream_connect_time\t $upstream_header_time\t$upstream_response_length\t$upstream_bytes_received\t'
      '$request_uri\t"$http_user_agent"\t'
      '$sent_http_x_xiaomi_error_code';
    ```
- 收集速率：日志采集时多长时间发送一次，控制发送的速率。
- 索引列：日志每一部分代表的含义，这个是从当前 store 中选择的，且顺序必须和日志中实际的日志中字段各部分的含义对应。store 的索引列必须是最多的，避免 tail 选择时没有。

### 4、查询日志

支持日志的全文搜索，支持v查询，k:v查询及and、or、not的组合查询。

![ozhera-log-query.png](images%2Fozhera-log-query.png)

查询语法详细说明：
- 关键词查询：直接输入关键词进行查询
  - 支持精确查询，例如："dispatcher“ 可搜索任意字段中包含dispatcher的日志数据(如果索引列类型是keyword则需要全部匹配)
  - 支持分词查询，例如：test log stream，则会分词查询
- 若条件包裹在双引号内，则只支持短语前缀匹配查询 message 字段或精确查询 traceId 字段，如 "b24e35ed31cb51e5516e25453228f36d" 可搜索 traceId 为条件，或 message 包含分词后以条件开头的词的日志数据
- kv 查询：输入 key:value 的形式进行查询
  - 若 value 是包裹在双引号内的，则前缀匹配查询 key 的值以 value 开头的日志数据
  - 否则，则匹配查询 key 的值为 value 的日志数据
    上述的匹配查询：即会对条件进行分词，然后再分别搜索分词后的各条件

### 5、与 trace 联动

解析后的日志包含 traceId 或 trace_id 字段时，会显示“查看链路”标签。

![ozhera-log-to-trace.png](images%2Fozhera-log-to-trace.png)

### 6、采集进度

在这可以看到，当前当前采集日志应用名，采集时间，采集路径，ip，行号，采集进度。

通过这个进度你可以看到你的日志被采集的是否合理，当采集进度长时间不正常可以找我们解决这个问题。

![ozhera-log-collect-process.png](images%2Fozhera-log-collect-process.png)

### 7、日志分析

![ozhera-log-analyze.png](images%2Fozhera-log-analyze.png)

可新建图表，选择左侧的字段就会按左侧字段进行分组统计，可以通过各种类型的图清晰的看到比重，有助于对日志做一些分析处理。

### 8、日志授权

![ozhera-log-shouquan2.png](images%2Fozhera-log-shouquan2.png)

选择给你要让展示的space授权，则授权的space也可以看到当前store。

### 9、资源隔离

首先，需要先创建资源。

![ozhera-log-resource.png](images%2Fozhera-log-resource.png)

添加RocketMQ资源：

![ozhera-log-resource-mq.png](images%2Fozhera-log-resource-mq.png)

**配置含义：**

- 别名：为你自己RocketMq信息的唯一标识，创建时希望用户可以创建的见名知意一些
- 区域：现在只有大陆机房
- mq地址:rocketMQ所在的地址
- MQ域名：没有域名，则可以不用填写
- ak、sk：rocketMq的密钥信息，如果没有可以不填
- brokerName：rocketMq队列的名列，默认为broker-0，如果自己做了rocketMQ的定制，则需要改
- 标签列表：你可以给你创建的这个信息打一些标签，用过这些标签可以唯一识别你们

添加ES资源：

![ozhera-log-resource-es.png](images%2Fozhera-log-resource-es.png)

**配置含义：**

- 别名：给你创建的所以给一个见名知意的名称
- 区域：选择属于哪个区域
- es集群名称：即你使用的es的集群名称
- esApi地址：es地址
- 连接方式：默认是用户名密码方式
- 用户名、密码：你是用的es的用户名和密码信息，没有可以不用填
- 标签列表：给你创建的这些信息打一些可以标识的标签信息
- es索引组：用户需要提前创建好索引，然后绑定到所属的日志类型，可以提前为每种类型创建一个索引，也可以多种类型共用一个索引

如果上边 2 个信息没有提前创建，在创建 store 的时候会让你来初始化这些信息，如果初始化完成，则需要选择你创建的这些信息即可。

## 五、基础设置

### （一）过滤配置

### 1、监控指标过滤设置

#### 1）排除操作内容

按指定操作内容排除，支持模糊匹配。这里的操作内容，指的是链路追踪详情中，每一个节点的方法，如下图所示：

![ozhera-filter-operation.png](images%2Fozhera-filter-operation.png)

#### 2）排除操作内容（http server）

有的时候，操作内容http client与server是相同的，这时候我们可能只希望排除http server的指标，这时候需要单独配置此项。例如：/test1

#### 3）排除线程名称

按线程名称排除，支持模糊匹配。例如：pool-1-thread。

#### 4）排除SQL

按SQL排除，支持模糊匹配。比如健康检查的SQL，可以配置：SELECT ?或者配置SELECT 1，具体可以在链路追踪详情中，SQL节点的tags里面查看。

#### 5）排除URL

按http url排除，支持模糊匹配。这里的url，是链路追踪详情中，HTTP节点的tags中的http.url所展示的。例如：/remotehealth1，或者是http://domain:port。

![ozhera-filter-url.png](images%2Fozhera-filter-url.png)

#### 6）排除User-Agent

按Http user-agent排除，支持模糊匹配。请求的User-Agent，可以在链路追踪Http节点的tags中查看。例如：chrome8.5，或者公司中安全扫描所携带的User-Agent。

![ozhera-filter-ua.png](images%2Fozhera-filter-ua.png)

### 2、异常、慢查询列表设置

#### 1）RPC耗时阈值

超过阈值的dubbo请求，会被认为是慢查询，展示在dubbo consumer/provider慢查询列表中。默认是1秒。

#### 2）DB耗时阈值

超过阈值的mysql请求，会被认为是慢查询，展示在mysql慢查询列表中。默认是1秒。

#### 3）排除Http异常码

http状态码不显示在异常列表，例如：400，表示400的请求不算异常请求。


