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

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TracePromptResult {

    // Detailed description of the exception reason, using professional Chinese terminology
    private String traceReason;

    // Name of the application that caused the exception,
    // obtained from process.serviceName of the problem node
    private String application;

    // Whether it is the root cause, determined based on analysis
    private boolean root;

    // project ID of the application.
    private String projectId;

    // Environment ID of the application,
    // obtained from the value of service.env.id in process.tags of the problem node
    private String envId;

    // Start time of the root cause Span,
    // must use the original value without any conversion
    private String startTime;

    // Duration of the root cause node, obtained from the duration of the root cause node,
    // must use the original value without any conversion
    private String duration;

    // Span ID of what you believe to be the root cause node
    private String spanId;

    // Value of process.ip for the node where the spanId is located
    private String ip;

    public static final String TRACE_REASON_KEY = "traceReason";
    public static final String APPLICATION = "application";
    public static final String ROOT = "root";
    public static final String PROJECT_ID = "projectId";
    public static final String ENV_ID = "envId";
    public static final String START_TIME = "startTime";
    public static final String DURATION = "duration";
    public static final String SPAN_ID = "spanId";
    public static final String IP = "ip";
}
