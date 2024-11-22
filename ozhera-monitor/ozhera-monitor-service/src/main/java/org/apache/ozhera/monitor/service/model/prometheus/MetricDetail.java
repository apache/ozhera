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

package org.apache.ozhera.monitor.service.model.prometheus;

import lombok.Data;

import java.io.Serializable;

/**
 * @author gaoxihui
 * @date 2021/9/3 9:51 上午
 */
@Data
public class MetricDetail implements Serializable {

    /**
     * 域：jaegerquery/china_tesla/youpin-tesla"
     */
    private String domain;

    /**
     * 应用信息
     * projectId_projectName
     */
    private String serviceName;

    /**
     *
     * serverIp
     */
    private String host;

    /**
     * 指标类型
     * http/dubbo/mysql
     */
    private String type;

    /**
     * 指标子类型：
     * error - 异常数据
     * timeout - 慢查询数据
     */
    private String errorType;

    /**
     * 异常码
     */
    private String errorCode;

    /**
     * 耗时
     */
    private String duration;

    /**
     * type-value
     * http-path;
     * dubbo-serviceName/methodName;
     * mysql-sql
     */
    private String url;

    /**
     * mysql
     */
    private String dataSource;

    private String traceId;

    private String timestamp;

    private Long createTime;

}
