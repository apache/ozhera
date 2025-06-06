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
package org.apache.ozhera.log.api.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class TraceLogQuery implements Serializable {
    private Long appId;
    private String ip;
    private String traceId;
    private String generationTime;
    private String level;
    // The maximum number of returned data bars
    private Integer total = 1000;
    // ES query expiration time (ms)
    private Long timeout = 2000L;

    public TraceLogQuery(Long appId, String ip, String traceId) {
        this.appId = appId;
        this.ip = ip;
        this.traceId = traceId;
    }

    public TraceLogQuery() {
    }
}
