# 概述
+ 数据抽取、加载和转换的系统。
+ trace-etl目前主要包含trace-etl-server，trace-etl-es，trace-etl-manager，trace-etl-nginx这四个模块。

# 模块功能介绍

## trace-etl-server

+ 将trace数据转换为metrics，通过HttpServer暴露http接口，Prometheus定时进行拉取。
+ 将错误、高耗时的trace详情存入ES或Doris。
+ 项目构建、运行可以参考：[trace-etl-server文档](trace-etl-server/README_CN.md)

## trace-etl-es

+ trace数据尾采样
+ 项目构建、运行可以参考：[trace-etl-es文档](trace-etl-es/README_CN.md)

## trace-etl-manager

+ 链路追踪的前端接口
+ 链路配置的创建、查询、下发
+ 项目构建、运行可以参考：[trace-etl-manager文档](trace-etl-manager/README_CN.md)

## trace-etl-nginx

+ 未来对于NGINX日志转换为trace、metrics的项目
+ 目前只抽取了转换接口

## trace-etl-extensions

+ 是trace-etl中接口对应多实现的扩展模块。比如MQ的RocketMQ和Kafka扩展；存储的Doris和ES扩展等。
+ 如何写一个扩展，可以参考：[trace-etl-extensions文档](docs/extension/extension_cn.md)