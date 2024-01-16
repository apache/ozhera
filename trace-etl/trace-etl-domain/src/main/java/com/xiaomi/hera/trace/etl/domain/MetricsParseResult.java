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
package com.xiaomi.hera.trace.etl.domain;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/10/27 10:09 上午
 */
public class MetricsParseResult {

    private JaegerTracerDomain jaegerTracerDomain;

    private DriverDomain driverDomain;

    private boolean ignore;

    private boolean isValidate;

    private HeraTraceEtlConfig heraTraceEtlConfig;

    public MetricsParseResult(boolean ignore){
        this.ignore = ignore;
    }

    public MetricsParseResult(JaegerTracerDomain jaegerTracerDomain, DriverDomain driverDomain, boolean ignore, boolean isValidate, HeraTraceEtlConfig heraTraceEtlConfig){
        this.isValidate = isValidate;
        this.ignore = ignore;
        this.jaegerTracerDomain = jaegerTracerDomain;
        this.driverDomain = driverDomain;
        this.heraTraceEtlConfig = heraTraceEtlConfig;
    }

    public HeraTraceEtlConfig getHeraTraceEtlConfig() {
        return heraTraceEtlConfig;
    }

    public void setHeraTraceEtlConfig(HeraTraceEtlConfig heraTraceEtlConfig) {
        this.heraTraceEtlConfig = heraTraceEtlConfig;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public JaegerTracerDomain getJaegerTracerDomain() {
        return jaegerTracerDomain;
    }

    public void setJaegerTracerDomain(JaegerTracerDomain jaegerTracerDomain) {
        this.jaegerTracerDomain = jaegerTracerDomain;
    }

    public DriverDomain getDriverDomain() {
        return driverDomain;
    }

    public void setDriverDomain(DriverDomain driverDomain) {
        this.driverDomain = driverDomain;
    }

    public boolean isValidate() {
        return isValidate;
    }

    public void setValidate(boolean validate) {
        isValidate = validate;
    }
}
