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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * @author gaoxihui
 * @date 2021/8/16 11:42 上午
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Metric implements Serializable {

    private String application;
    private String instance;
    private String ip;
    private String job;
    private String replica;
    private String serverIp;

    //dubbo label
    private String methodName;
    private String serviceName;

    //sql
    private String sqlMethod;
    private String sql;
    private String dataSource;

    //redis
    private String host;
    private String port;
    private String dbindex;
    private String method;

    //env
    private String serverEnv;
    private String serverZone;

    //container info
    private String container_label_PROJECT_ID;
    private String name;//
    private String container;//
    private String containerName;
    private String pod;//
    private String podIp;//
    private String namespace;//

    private String lastCreateTime;

    private double value;

    private String timestamp;

    private String traceId;

    private String service; //应用的服务维度

    //下游服务信息
    private String clientProjectId;
    private String clientProjectName;
    private String clientEnv;
    private String clientIp;

    private String group;
    private String url;

    //topology
    private String type;
    private String destApp;

    // http client and server error status code
    private String errorCode;
}
