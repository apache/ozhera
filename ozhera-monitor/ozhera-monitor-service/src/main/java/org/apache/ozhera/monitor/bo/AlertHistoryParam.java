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

import lombok.Data;

/**
 *
 * @author zhanggaofeng1
 */
@Data
public class AlertHistoryParam {

    private Long startTime;
    private Long endTime;
    private Integer page;
    private Integer pageSize;
    private String projectId;
    private Integer iamTreeId;
    private String alertLevel;
    private String serverIp;
    private String instance;
    private String methodName;
    private String id;
    private String alertName;
    private String comment;
    private String alertStat = "firing";

    public void pageQryInit() {
        if (page == null || page <= 0) {
            page = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
        if (pageSize >= 100) {
            pageSize = 99;
        }
        if (endTime == null) {
            endTime = System.currentTimeMillis();
        }
        if (startTime == null) {
            startTime = endTime - 24L * 3600L * 1000L;
        }
    }

}
