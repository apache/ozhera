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
package org.apache.ozhera.monitor;

import java.util.Arrays;
import java.util.List;

/**
 * @author zhangxiaowei6
 * @date 2023-02-22
 */
public class DashboardConstant {
    public static final String DEFAULT_FOLDER_NAME = "Hera";
    public static final String GRAFANA_API_KEY_NAME = "hera";
    public static final String GRAFANA_API_KEY_ROLE = "Admin";
    public static final String GRAFANA_USER_NAME = "admin";
    public static final String GRAFANA_PASSWORD = "admin";
    public static final String GRAFANA_DATASOURCE_URL = "http://prometheus";
    public static final String GRAFANA_DATASOURCE_NAME = "Prometheus";
    public static final String GRAFANA_DATASOURCE_TYPE = "prometheus";

    public static final String GRAFANA_FOLDER_UID = "Hera";

    public static List<String> GRAFANA_SRE_TEMPLATES = Arrays.asList("nodeMonitor", "dockerMonitor", "serviceMarket",
            "resourceUtilization", "dubboProviderOverview", "dubboProviderMarket", "dubboConsumerOverview",
            "dubboConsumerMarket", "httpServerMarket", "httpServerOverview", "heraSLA", "grpcConsumerMarket",
            "grpcConsumerOverview", "grpcProviderMarket", "grpcProviderOverview");
    public static final String JAEGER_QUERY_File_NAME = "jaegerQuery.ftl";
    public static final String GOLANG_File_NAME = "golang.ftl";
    public static final String DEFAULT_PANEL_ID_LIST = "110,148,152,112,116,118,150,122,120,126,124,130,128,132,134," +
            "136,138,140,142,144,146,52,56,58,60,66,95,96,50,82,68,78,74,76,102,104,106,146,159,163,168,169,170,171," +
            "172,173,174,176,178";

    public static final String DEFAULT_GOLANG_ID_LIST = "110,148,152,112,116,120,124,128,132,134," +
            "136,138,140,142,144,146,66,95,96,50,82,68,78,102,104,106,146,159,170,171," +
            "172,173,174,150,126,130,122,118,163,169,168";
    public static final String DEFAULT_JAEGER_QUERY_JOB_NAME = "jaeger-query";
    public static final String DEFAULT_JVM_JOB_NAME = "mione-yewujiankong-china-jvm";
    public static final String DEFAULT_DOCKER_JOB_NAME = "mione-china-cadvisor-k8s";
    public static final String DEFAULT_NODE_JOB_NAME = "mione-china-node-k8s";
    public static final String DEFAULT_CUSTOMIZE_JOB_NAME = "mione-china-customize";

    public static final String DEFAULT_GOLANG_RUNTIME_JOB_NAME = "mione-golang-runtime";

    public static final String DEFAULT_MIMONITOR_NACOS_CONFIG = "mimonitor_open_config";
    public static final String DEFAULT_MIMONITOR_NACOS_GROUP = "DEFAULT_GROUP";

    public static final String EXCEPTION_TRACE_DOMAIN_JAEGER = "jaegerquery";

    public static final String EXCEPTION_TRACE_DOMAIN_HERA = "hera";

    public static final String HERA_METRICS_PREFIX = "hera";

    public static final String HERA_METRICS_INNER_PREFIX = "jaeger";


}
