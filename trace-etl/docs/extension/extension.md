# Extension Mechanism

Within `trace-etl`, Extensions are used to extend functionalities.

All Extensions are stored in the `trace-etl-extensions` module.

# Extension Development

When a new extension needs to be added, we should follow the specifications below for development:

## 1. Create a new module

You need to create a new module under `trace-etl-extension`. The naming convention is `trace-etl-${custom-name}-extension`. For instance, the extension for RocketMQ should be named: `trace-etl-rocketmq-extension`.

## 2. Implement the interface

The `trace-etl-extensions` module contains all extension implementations. Each extension needs to implement its corresponding interface. These interfaces are located in the `trace-etl-api` directory. For example, the implementation class for the RocketMQ Extension is in the `trace-etl-rocketmq-extension` module named RocketMQExtension, which internally implements the MQExtension interface.

Each interface might have one or more implementations.

## 3. Complete the interface implementation

The Extension implementation class that we develop should implement all methods in the interface, even if some methods are not currently used. This is because these are the fundamental methods for using this framework or middleware, and they might be used in the future.

Of course, apart from the basic methods, the specific logic of trace-etl also needs to be implemented. For example, in the MQExtension, there's a `sendByTraceId` method that requires hashing based on the traceID to ensure messages with the same traceID are sent to the same consumer instance.

## 4. Use the Extension

### (1) Import Extension Dependency

When using an Extension in a project, you first need to import the dependency of the newly created Extension module.

### (2) Configure the Extension

When using the Extension in a project, under the `src/main/resource/` directory, in the `application.properties` file, configure it under the key named `extensions`. The value's format is as follows:

`${Extension1Name}:${Extension1ImplementationClassName}:${Extension1PackagePath},${Extension2Name}:${Extension2ImplementationClassName}:${Extension2PackagePath},...`

Explanation:

Each Extension has a corresponding implementation class and the package path of this class. They are separated by colons. Multiple Extensions are separated by commas.

`${Extension1Name}`: This is custom-defined and is a unique identifier for the Extension. It is used when fetching the Extension implementation in code.

`${Extension1ImplementationClassName}`: This should match the value in the `@Service` annotation of the Extension implementation class, indicating which Extension implementation class we want to use.

`${Extension1PackagePath}`: This represents the package path of the Extension implementation class. The Extension framework needs this when loading the Extension implementation.

For example:

The current MQ Extension has an implementation class named `RocketMQExtension`. The value of its `@Service` annotation is `rocketMQ`, and its package path is `run.mone.trace.etl.extension.rocketmq`. Thus, its configuration would be:

```properties
extensions=mq:rocketMQ:run.mone.trace.etl.extension.rocketmq
```

### Using the Extension

Using the Extension in the code is straightforward. We need to inject the `run.mone.docean.spring.extension.Extensions` class into the class where we are using it. After that, use the `get(String)` method from `Extensions` in the code to fetch the Extension implementation class. The parameter for the `get(String)` method is the custom name of the Extension that we defined in the configuration file.

For example:

In the configuration file, our setting is:

```properties
extensions=mq:rocketMQ:run.mone.trace.etl.extension.rocketmq
```

In the code, we retrieve and use the Extension as follows:

```java

@Service
@Slf4j
public class ConsumerService {

    // Inject `Extensions`, which manages the Extensions.
    @Resource
    private Extensions extensions;

    @PostConstruct
    public void takeMessage() throws MQClientException {
        // Retrieve the Extension implementation class. 
        // The "mq" parameter here must match the name of the Extension defined in the configuration file.
        MQExtension<MessageExt> mq = extensions.get("mq");

        MqConfig<MessageExt> config = new MqConfig<>();

        // Invoke the `initMq` method of the Extension.
        mq.initMq(config);
    }


}

```