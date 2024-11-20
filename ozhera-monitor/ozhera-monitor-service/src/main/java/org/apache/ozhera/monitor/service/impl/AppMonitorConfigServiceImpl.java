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

package org.apache.ozhera.monitor.service.impl;

import org.apache.ozhera.monitor.dao.AppMonitorConfigDao;
import org.apache.ozhera.monitor.dao.model.AppMonitorConfig;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.AppMonitorConfigService;
import org.apache.ozhera.monitor.service.model.PageData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author gaoxihui
 * @date 2021/8/19 4:07 PM
 */
@Slf4j
@Service
public class AppMonitorConfigServiceImpl implements AppMonitorConfigService {
    
    @Autowired
    AppMonitorConfigDao dao;
    
    @Override
    public Result<String> createConfig(AppMonitorConfig config) {
        config.setCreateTime(new Date());
        config.setUpdateTime(new Date());
        config.setStatus(0);
        int i = dao.create(config);
        if (i < 1) {
            return Result.fail(ErrorCode.unknownError);
        }
        return Result.success(null);
    }
    
    @Override
    public Result<String> updateConfig(AppMonitorConfig config) {
        config.setStatus(0);
        config.setUpdateTime(new Date());
        int update = dao.update(config);
        if (update < 1) {
            return Result.fail(ErrorCode.unknownError);
        }
        return Result.success(null);
    }
    
    @Override
    public Result<String> delConfig(Integer id) {
        AppMonitorConfig config = dao.getById(id);
        if (config == null) {
            log.error("AppMonitorConfigService.delConfig error! no config data found By id : {}", id);
            return Result.fail(ErrorCode.unknownError);
        }
        config.setStatus(1);
        config.setUpdateTime(new Date());
        int update = dao.update(config);
        if (update < 1) {
            log.error("AppMonitorConfigService.delConfig failed! id : {}", id);
            return Result.fail(ErrorCode.unknownError);
        }
        return Result.success(null);
    }
    
    @Override
    public Result<PageData> getConfig(Integer projectId, Integer type, String configName, Integer status, Integer page,
            Integer pageSize) {
        
        try {
            return dao.getConfig(projectId, type, configName, status, page, pageSize);
        } catch (Exception e) {
            log.error("AppMonitorConfigService.getConfig error : {}", e.getMessage());
            return Result.fail(ErrorCode.unknownError);
        }
    }
    
    
}
