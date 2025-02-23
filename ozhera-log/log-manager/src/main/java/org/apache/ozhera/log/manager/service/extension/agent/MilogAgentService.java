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
package org.apache.ozhera.log.manager.service.extension.agent;

import org.apache.ozhera.log.api.model.meta.LogCollectMeta;
import org.apache.ozhera.log.api.model.vo.AgentLogProcessDTO;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.model.bo.MilogAgentIpParam;

import java.util.List;

public interface MilogAgentService {

    String DEFAULT_AGENT_EXTENSION_SERVICE_KEY = "defaultAgentExtensionService";

    String LOG_PATH_PREFIX = "/home/work/log";

    Result<List<AgentLogProcessDTO>> process(String ip);

    Result<String> configIssueAgent(String agentId, String agentIp, String agentMachine);

    void publishIncrementConfig(Long tailId, Long milogAppId, List<String> ips);

    void publishIncrementDel(Long tailId, Long milogAppId, List<String> ips);

    void delLogCollDirectoryByIp(Long tailId, String directory, List<String> ips);

    Result<String> agentOfflineBatch(MilogAgentIpParam agentIpParam);

    LogCollectMeta getLogCollectMetaFromManager(String ip);
}
