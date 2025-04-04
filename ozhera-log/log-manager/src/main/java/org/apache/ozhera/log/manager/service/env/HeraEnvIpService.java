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
package org.apache.ozhera.log.manager.service.env;

import org.apache.ozhera.log.api.model.meta.LogPattern;
import org.apache.ozhera.log.manager.model.vo.LogAgentListBo;

import java.util.List;
import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/15 19:25
 */
public interface HeraEnvIpService {
    /**
     * Query all pod information in the node
     *
     * @param nodeIp
     * @return
     */
    List<LogAgentListBo> queryInfoByNodeIp(String nodeIp);

    Map<String, List<LogAgentListBo>> queryAgentIpByPodIps(List<String> podIps);

    /**
     * Query the node IP based on the pode IP
     *
     * @param ips
     * @return
     */
    List<LogPattern.IPRel> queryActualIps(List<String> ips, String agentIp, String logPath);
}
