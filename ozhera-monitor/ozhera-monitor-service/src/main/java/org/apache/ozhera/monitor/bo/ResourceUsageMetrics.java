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
package org.apache.ozhera.monitor.bo;

import org.apache.commons.lang3.StringUtils;

/**
 * @author gaoxihui
 */
public enum ResourceUsageMetrics {
    dockerResourceUsage("dockerResourceUsage","docker资源利用率", "3",AlarmPresetMetrics.container_cpu_resource_use_rate, AlarmPresetMetrics.container_mem_resource_use_rate),
    k8sResourceUsage("k8sResourceUsage","k8s资源利用率", "4",AlarmPresetMetrics.k8s_cpu_resource_use_rate,AlarmPresetMetrics.k8s_mem_resource_use_rate,AlarmPresetMetrics.k8s_cpu_avg_use_rate),
    ;
    private String code;
    private String message;
    private String metricsFlag;

    private AlarmPresetMetrics[] metrics;

    ResourceUsageMetrics(String code,String message,String metricsFlag, AlarmPresetMetrics... metrics){
        this.code = code;
        this.message = message;
        this.metricsFlag = metricsFlag;
        this.metrics = metrics;
    }

    public String getCode() {
        return code;
    }

    public AlarmPresetMetrics[] getMetrics() {
        return metrics;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMetricsFlag() {
        return metricsFlag;
    }

    public void setMetricsFlag(String metricsFlag) {
        this.metricsFlag = metricsFlag;
    }

    public static ResourceUsageMetrics getErrorMetricsByMetrics(String metrics) {
        if (StringUtils.isBlank(metrics)) {
            return null;
        }
        for (ResourceUsageMetrics errMetrics : ResourceUsageMetrics.values()) {
            if (errMetrics.metrics == null || errMetrics.metrics.length == 0) {
                continue;
            }
            for (AlarmPresetMetrics ele : errMetrics.metrics) {
                if (ele.getCode().equals(metrics)) {
                    return errMetrics;
                }
            }
        }
        return null;
    }

    public static String getMetricsFlagByMetrics(String metrics) {
        if (StringUtils.isBlank(metrics)) {
            return null;
        }
        for (ResourceUsageMetrics errMetrics : ResourceUsageMetrics.values()) {
            if (errMetrics.metrics == null || errMetrics.metrics.length == 0) {
                continue;
            }
            for (AlarmPresetMetrics ele : errMetrics.metrics) {
                if (ele.getCode().equals(metrics)) {
                    return errMetrics.getMetricsFlag();
                }
            }
        }
        return null;
    }

}
