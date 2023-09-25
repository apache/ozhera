# Extension机制

在trace-etl中，使用Extension来对功能进行扩展。

所有的Extension都存放在`trace-etl-extensions`module中。

# Extension开发

当需要有新的扩展添加进来时，需要我们按照以下规范来开发：

## 1、新建module

需要在`trace-etl-extension`下新建modulr，命名格式是以`trace-etl-${自定义}-extension`格式。例如，RocketMQ的扩展，就命名为：`trace-etl-rocketmq-extension`。

## 2、实现接口

`trace-etl-extensions`module中存放的都是扩展实现，每个扩展实现都需要实现对应的接口。接口在`trace-etl-api`目录中存放。例如：RocketMQ Extension的实现类为：`trace-etl-rocketmq-extension`module中的RocketMQExtension，它内部实现了MQExtension这个接口。

每一个接口都会有一个或多个实现。

## 3、完成接口实现

我们开发的Extension实现类需要实现所有接口中的方法，虽然有些方法目前没有用到。因为他们是最基本的使用这个框架、中间件的方法，在未来很可能会被使用。

当然，除了基本方法外，还要实现trace-etl的特有逻辑。例如：在MQExtension中，有一个`sendByTraceId`方法，需要按照traceID进行hash，将相同traceID的信息发送到同一个consumer实例中。

## 4、使用Extension

### （1）导入Extension依赖

在使用Extension的工程，需要先导入新建的Extension module的依赖。

### （2）配置Extension

在使用Extension的工程，在`src/main/resource/`目录下，`application.properties`中，key为`extensions`的配置中进行配置。它的值的格式如下：

`${Extension1的名字}:${Extension1的实现类的name}:${Extension1的实现类所在的包路径},${Extension2的名字}:${Extension2的实现类的name}:${Extension2的实现类所在的包路径},······`

解释如下：

每一个Extension都有一个对应的实现类，以及实现类的包路径。他们之间以英文冒号分隔。多个Extension以英文逗号分隔。

`${Extension1的名字}`：他是我们自定义的，是这个Extension的唯一标识，在代码获取Extension实现的时候需要用到。

`${Extension1的实现类的name}`：他是与Extension实现类上，@Service注解中的值保持一致，表示的是我们需要使用哪个Extension实现类。

`${Extension1的实现类所在的包路径}`：他是Extension实现类所在的包路径，是Extension框架加在Extension实现的时所需要的。

例如：

目前MQ Extension有一个实现类是`RocketMQExtension`，这个类的@Service注解的值为`rocketMQ`，这个类所在的包路径为`run.mone.trace.etl.extension.rocketmq`，所以，他的配置就为：

```properties
extensions=mq:rocketMQ:run.mone.trace.etl.extension.rocketmq
```

### 使用Extension

在代码中使用Extension很简单，我们需要在使用的类中注入`run.mone.docean.spring.extension.Extensions`类，然后在代码中使用`Extensions`中的`get(String)`方法，来获取Extension实现类。get(String)方法的参数，就是我们在配置文件中自定义的Extension的name。

例如：

配置文件中，我们的配置为：

```properties
extensions=mq:rocketMQ:run.mone.trace.etl.extension.rocketmq
```

代码中，我们获取使用Extension：

```java

@Service
@Slf4j
public class ConsumerService {

    // 注入Extensions，他对Extension进行管理
    @Resource
    private Extensions extensions;

    @PostConstruct
    public void takeMessage() throws MQClientException {
        // 获取Extension实现类，这里的“mq”参数，必须要与配置文件中的Extension的名字保持一致
        MQExtension<MessageExt> mq = extensions.get("mq");

        MqConfig<MessageExt> config = new MqConfig<>();

        // 调用Extension的initMq方法
        mq.initMq(config);
    }


}

```