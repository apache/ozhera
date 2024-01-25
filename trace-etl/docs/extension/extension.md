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

## 4. Add Implementation Comments

Add the `org.springframework.stereotype.Service` annotation to the implementation class, and include `@ConditionalOnProperty(name = "Interface Function Type", havingValue = "Specific Implementation")`. This `ConditionalOnProperty` annotation is used to select a specific implementation class, loading only that implementation. For example: `@ConditionalOnProperty(name = "mq.type", havingValue = "kafka")` signifies that this class is an MQ interface implementation, with Kafka as its specific implementation.

Sometimes, our implementation classes have dependencies on many other classes. For instance, in `trace-etl-doris-extension`, there are `DorisDataSourceService`, `QueryDorisService`, and `WriteDorisService`. To simplify operations, a `config` package is added. `DorisConfig` in this package is responsible for initializing the basic invocation class (`DorisService`), the actual implementation class (`DorisDataSourceService`), and the dependent classes of the implementation (`QueryDorisService`, `WriteDorisService`). Simply add `@Service` and `@ConditionalOnProperty` annotations to `DorisConfig`. No additional annotations are required on other classes. They are instantiated through the `@Bean` annotation in `DorisConfig` and added to the Spring container.


## 5. Use the Extension

### (1) Import Extension Dependency

When using an Extension in a project, you first need to import the dependency of the newly created Extension module.

### (2) Configure Extension

For projects utilizing an Extension, within the `src/main/resources/` directory, find the `application.properties` file. Here, the key is `Interface Function Type`, and the value is the `Specific Implementation`. For example:

```properties
mq.type=kafka
storage.type=doris
```

Explanation:

This signifies that the current MQ being used is Kafka, and the storage in use is Doris. Only the implementation classes related to Kafka and Doris will be instantiated.