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
package org.apache.ozhera.intelligence.domain.rootanalysis;

import lombok.Data;

import java.io.Serializable;

@Data
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

    //container info
    private String container_label_PROJECT_ID;
    private String name;//container name eg:zzytest-20220323221926067
    private String container;//container name for k8s eg:youpin-gateway-dingtao-test2-900329(gitGroup-gitName-envId)
    private String pod;//pod name for k8s eg:91102-dingtao-test-644758f5b4-jk9t7
    private String namespace;//namespace name for k8s eg:nrme

    private String lastCreateTime;

    private double value;

    private String timestamp;

    private String traceId;

    private String service; //应用的服务维度（云平台）

    private String apiid;

    private String serialid;

    private String sceneid;

    private String pod_ip;

    private String calert;

    private String alert_op;

    private String alert_value;

    private String detailRedirectUrl;

    private String __priority__;

    //下游服务信息
    private String clientProjectId;
    private String clientProjectName;
    private String clientEnv;
    private String clientIp;

}
