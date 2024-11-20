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

package org.apache.ozhera.monitor.service.helper;

import org.apache.ozhera.log.api.model.dto.MontorAppDTO;
import org.apache.ozhera.log.api.service.MilogOpenService;
import org.apache.ozhera.monitor.bo.PlatForm;
import org.apache.ozhera.monitor.dao.AppMonitorDao;
import org.apache.ozhera.monitor.dao.model.AppMonitor;
import org.apache.ozhera.monitor.service.extension.PlatFormTypeExtensionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @project: mimonitor
 * @author: zgf1
 * @date: 2021/12/9 10:30
 */
@Slf4j
@Component
public class ProjectHelper {

    @Reference(check = false, interfaceClass = MilogOpenService.class, group = "${dubbo.group}")
    private MilogOpenService milogOpenService;
    @Autowired
    private AppMonitorDao appMonitorDao;

    @Autowired
    private PlatFormTypeExtensionService platFormTypeExtensionService;

    /**
     * 是否入住日志系统
     *
     * @param projectId
     * @return
     */
    public boolean accessLogSys(String projectName, Long projectId, Integer appSource) {
        log.info("查询详情是否入住日志系统请求 projectName={}, projectId={}", projectName, projectId);
        if (StringUtils.isBlank(projectName) || projectId == null) {
            return true;
        }

        if (platFormTypeExtensionService.belongPlatForm(appSource, PlatForm.miCloud)) {
            return true;
        }

        AppMonitor app = appMonitorDao.getByAppIdAndName(projectId.intValue(), projectName);
        if (app == null) {
            log.info("查询详情是否入住日志系统请求，没有app信息; projectId={}, projectName={}", projectId, projectName);
            return true;
        }
        try {
            MontorAppDTO result = milogOpenService.queryHaveAccessMilog(app.getIamTreeId().longValue(), String.valueOf(projectId), appSource);
            log.info("查询详情是否入住日志系统请求iamId:{},projectId:{},appSource:{},响应 result={}", app.getIamTreeId(), projectId, appSource, result);
            if (result == null) {
                return false;
            }
            return result.getIsAccess();
        } catch (Exception e) {
            log.error("查询项目是否入住日志系统异常; app={}", app, e);
            return true;
        }
    }

}
