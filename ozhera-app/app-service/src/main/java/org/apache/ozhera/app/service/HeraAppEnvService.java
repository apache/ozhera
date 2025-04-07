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
package org.apache.ozhera.app.service;

import org.apache.ozhera.app.model.vo.HeraAppEnvVo;
import org.apache.ozhera.app.model.vo.HeraAppOperateVo;

import java.util.List;

public interface HeraAppEnvService {

    HeraAppEnvVo queryAppEnvById(Long id);

    Long addAppEnv(HeraAppOperateVo operateVo);

    Long updateAppEnv(HeraAppOperateVo operateVo);

    Boolean deleteAppEnv(Long id);

    void fetchIpsOpByApp(String app);

    void addAppEnvNotExist(HeraAppEnvVo heraAppEnvVo);

    Boolean addAppEnvNotExist(HeraAppEnvVo heraAppEnvVo, HeraAppEnvVo.EnvVo envVo);

    List<String> queryNonProbeAccessIPs();
}
