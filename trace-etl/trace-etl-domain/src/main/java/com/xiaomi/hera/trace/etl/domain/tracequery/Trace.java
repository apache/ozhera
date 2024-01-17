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
package com.xiaomi.hera.trace.etl.domain.tracequery;

import com.xiaomi.hera.trace.etl.domain.jaegeres.JaegerProcess;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/11/7 4:07 下午
 */
public class Trace {
    private String traceID;
    private List<Span> spans;
    private Map<String, JaegerProcess> processes;
    private String source;
    private String area;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getTraceID() {
        return traceID;
    }

    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }

    public List<Span> getSpans() {
        return spans;
    }

    public void setSpans(List<Span> spans) {
        this.spans = spans;
    }

    public Map<String, JaegerProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(Map<String, JaegerProcess> processes) {
        this.processes = processes;
    }
}
