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
package org.apache.ozhera.log.api.model.meta;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author shanwb
 * @date 2021-07-08
 */
@Data
public class LogCollectMeta implements Serializable {
    /**
     * agent mark
     */
    private String agentId;
    /**
     * agent machine name
     */
    private String agentMachine;
    /**
     * agent physical machine ip
     */
    private String agentIp;
    /**
     * Application metadata collected by the agent
     */
    private List<AppLogMeta> appLogMetaList;

    private AgentDefine agentDefine;

    /**
     * A single configuration data is configured by default to the full configuration of the machine
     */
    private Boolean singleMetaData;

    private String podType;

    /**
     * Log collection in this directory that needs to be deleted when a machine is offline has a value only when
     * the machine of an application is offline
     */
    private String delDirectory;

}
