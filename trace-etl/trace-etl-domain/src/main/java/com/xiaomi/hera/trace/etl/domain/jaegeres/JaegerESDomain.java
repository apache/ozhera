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
package com.xiaomi.hera.trace.etl.domain.jaegeres;

import java.util.List;

public class JaegerESDomain {

    private String traceID;
    private String spanID;
    private String operationName;
    private List<JaegerReferences> references;
    private long startTime;
    private long startTimeMillis;
    private long duration;
    private List<JaegerAttribute> tags;
    private List<JaegerLogs> logs;
    private JaegerProcess process;

    public String getTraceID() {
        return traceID;
    }

    public void setTraceID(String traceID) {
        this.traceID = traceID;
    }

    public String getSpanID() {
        return spanID;
    }

    public void setSpanID(String spanID) {
        this.spanID = spanID;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public List<JaegerReferences> getReferences() {
        return references;
    }

    public void setReferences(List<JaegerReferences> references) {
        this.references = references;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public void setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<JaegerAttribute> getTags() {
        return tags;
    }

    public void setTags(List<JaegerAttribute> tags) {
        this.tags = tags;
    }

    public List<JaegerLogs> getLogs() {
        return logs;
    }

    public void setLogs(List<JaegerLogs> logs) {
        this.logs = logs;
    }

    public JaegerProcess getProcess() {
        return process;
    }

    public void setProcess(JaegerProcess process) {
        this.process = process;
    }
}
