/*
 * Copyright 2020 Xiaomi
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
package com.xiaomi.youpin.prometheus.agent.result.alertManager;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Labels {
    private String alert_key;
    private String alert_op;
    private String alert_value;
    private String alertname;
    private String app_iam_id;
    private String application;
    private String calert;
    private String exceptViewLables;
    private String group_key;
    private String methodName;
    private String metrics;
    private String metrics_flag;
    private String project_id;
    private String project_name;
    private String send_interval;
    private String serverEnv;
    private String serverIp;
    private String serviceName;
    private String system;
    private String container;
    private String image;
    private String instance;
    private String ip;
    private String job;
    private String name;
    private String namespace;
    private String pod;
    private String restartCounts;
    private String detailRedirectUrl;
}
