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

## 4、添加实现注释

在实现类上添加org.springframework.stereotype.Service注解，并且添加@ConditionalOnProperty(name = "接口功能类型", havingValue = "具体实现")，这个ConditionalOnProperty
注解是为了选择具体实现类，从而只加载这个实现类。例如：@ConditionalOnProperty(name = "mq.type", havingValue = "kafka")，这个注解的含义是：这个类是一个MQ的接口实现类，它的具体实现
是kafka。

有时，我们的实现类关联依赖了很多类，比如在trace-etl-doris-extension中，有DorisDataSourceService\QueryDorisService\WriteDorisService，此时，我们为了简化操作，就添加了一个config包，
这个包下的DorisConfig负责初始化基础的调用类（DorisService）、真正的实现类（DorisDataSourceService）、实现类的依赖类（QueryDorisService、WriteDorisService），我们只需要在DorisConfig
上添加@Service注解和@ConditionalOnProperty注解即可。其他类上都不用添加相关注解，他们是通过DorisConfig中的@Bean注解实例化，并添加到Spring容器中的。

## 5、使用Extension

### （1）导入Extension依赖

在使用Extension的工程，需要先导入新建的Extension module的依赖。

### （2）配置Extension

在使用Extension的工程，在`src/main/resource/`目录下，`application.properties`中，key为`接口功能类型`，值为`具体实现`。例如：

`mq.type=kafka`
`storage.type=doris`

解释如下：

这代表着目前使用的MQ是kafka，使用的存储是Doris。只会实例化kafka和Doris相关的实现类。
