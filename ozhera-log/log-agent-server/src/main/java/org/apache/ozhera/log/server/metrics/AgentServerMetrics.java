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
package org.apache.ozhera.log.server.metrics;

import org.apache.ozhera.metrics.api.Metrics;
import org.apache.commons.lang3.StringUtils;

/**
 * Metrics utility for log-agent-server config publish paths.
 * <p>
 * All methods are static and wrapped in try/catch to avoid impacting the main flow.
 */
public final class AgentServerMetrics {

    // Status values
    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAIL = "fail";
    private static final String UNKNOWN = "unknown";

    // Metric names
    private static final String METRIC_STRATEGY_SEND_TOTAL = "agent_server_strategy_send_total";

    // Label names
    private static final String LABEL_STATUS = "status";
    private static final String LABEL_AGENT_IP = "agent_ip";
    private static final String LABEL_TAIL_ID = "tail_id";

    private AgentServerMetrics() {
    }

    /**
     * Increment strategy send total counter.
     *
     * @param status  {@link #STATUS_SUCCESS} or {@link #STATUS_FAIL}
     * @param agentIp the target agent IP
     * @param tailId  the logtail ID
     */
    public static void incrementStrategySendTotal(String status, String agentIp, String tailId) {
        try {
            Metrics.counter(METRIC_STRATEGY_SEND_TOTAL)
                    .tag(LABEL_STATUS, normalize(status))
                    .tag(LABEL_AGENT_IP, normalize(agentIp))
                    .tag(LABEL_TAIL_ID, normalize(tailId))
                    .inc();
        } catch (Exception ignored) {
            // Avoid impacting main flow if metrics fails
        }
    }

    private static String normalize(String value) {
        return StringUtils.isBlank(value) ? UNKNOWN : value;
    }
}
