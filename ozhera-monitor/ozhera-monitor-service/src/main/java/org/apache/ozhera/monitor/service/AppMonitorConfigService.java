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

package org.apache.ozhera.monitor.service;

import org.apache.ozhera.monitor.dao.model.AppMonitorConfig;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.model.PageData;

/**
 * @author gaoxihui
 * @date 2021/8/19 4:07 PM
 */

public interface AppMonitorConfigService {
    
    
    Result<String> createConfig(AppMonitorConfig config);
    
    Result<String> updateConfig(AppMonitorConfig config);
    
    Result<String> delConfig(Integer id);
    
    Result<PageData> getConfig(Integer projectId, Integer type, String configName, Integer status, Integer page,
            Integer pageSize);
    
    
}
