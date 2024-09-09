/*
 * Copyright (C) 2020 Xiaomi Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.hera.trace.etl.constant;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/4/2 9:41 上午
 */
public class SpanType {
    public static final String HTTP = "aop";
    public static final String DUBBO = "dubbo";
    public static final String MYSQL = "mysql";
    public static final String MONGODB = "mongodb";
    public static final String REDIS = "redis";
    public static final String ROCKETMQ = "rocketmq";
    public static final String CUSTOMIZE_MTTHOD = "customizeMethod";
    public static final String GRPC = "grpc";
}
