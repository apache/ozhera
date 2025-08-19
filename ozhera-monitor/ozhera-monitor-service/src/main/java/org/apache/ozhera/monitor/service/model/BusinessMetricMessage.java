/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ozhera.monitor.service.model;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

/**
 * 业务指标消息模型 - 重构版
 */
@Data
@ToString
public class BusinessMetricMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 唯一标识
     */
    private String id;

    /**
     * 原始消息内容
     */
    private String rawMessage;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 服务节点ip
     */
    private String serviceIp;

    /**
     * 服务环境
     */
    private String serviceEnv;

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 指标ID
     */
    private Long metricId;

    /**
     * 指标类型(如counter)
     */
    private String metricType;

    /**
     * 指标数据(JSON解析结果)
     */
    private Map<String, Object> metricData;

    /**
     * HeraSDK 设置时间
     */
    private Long sdkTimestamp;

    /**
     * 业务日志打印时间
     */
    private Long logTimestamp;

    /**
     * 当前系统接收时间
     */
    private Long receivedTimestamp;
}