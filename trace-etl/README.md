# Overview
+ A system for data extraction, loading, and transformation.
+ Currently, trace-etl mainly consists of four modules: trace-etl-server, trace-etl-es, trace-etl-manager, and trace-etl-nginx.

# Module Introduction

## trace-etl-server

+ Transforms trace data into metrics, exposes HTTP interfaces through HttpServer, and is pulled periodically by Prometheus.
+ Stores details of errors and high-latency traces in Elasticsearch (ES) or Doris.
+ For project build and execution, refer to: [trace-etl-server documentation](trace-etl-server/README.md)

## trace-etl-es

+ Tail sampling for trace data.
+ For project build and execution, refer to: [trace-etl-es documentation](trace-etl-es/README.md)

## trace-etl-manager

+ Frontend interface for link tracking.
+ Creation, query, and distribution of link configurations.
+ For project build and execution, refer to: [trace-etl-manager documentation](trace-etl-manager/README.md)

## trace-etl-nginx

+ Future project for converting NGINX logs into trace and metrics.
+ Currently, only the extraction and conversion interface is implemented.

## trace-etl-extensions

+ This is the extension module in trace-etl, allowing multiple implementations for corresponding interfaces. For example, extensions for MQ such as RocketMQ and Kafka, and extensions for storage like Doris and ES.
+ To learn how to write an extension, refer to: [trace-etl-extensions documentation](docs/extension/extension.md)
