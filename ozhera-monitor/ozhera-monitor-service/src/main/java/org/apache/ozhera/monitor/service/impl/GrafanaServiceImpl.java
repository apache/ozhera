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

import com.google.gson.JsonArray;
import org.apache.ozhera.monitor.dao.model.GrafanaTemplate;
import org.apache.ozhera.monitor.service.GrafanaService;
import org.apache.ozhera.monitor.service.api.GrafanaServiceExtension;
import org.apache.ozhera.monitor.service.model.MutiGrafanaResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author zhangxiaowei6
 */
@Slf4j
@Service
public class GrafanaServiceImpl implements GrafanaService {
    
    @Autowired
    GrafanaServiceExtension grafanaServiceExtension;
    
    @Override
    public void setFolderData(String area) {
        grafanaServiceExtension.setFolderData(area);
    }
    
    @Override
    public void setContainerAndHostUrl(String area) {
        grafanaServiceExtension.setContainerAndHostUrl(area);
    }
    
    @Override
    public String requestGrafana(String serverType, String appName, String area) {
        return grafanaServiceExtension.requestGrafana(serverType, appName, area);
    }
    
    @Override
    public MutiGrafanaResponse requestGrafanaTemplate(String group, String title, String area, GrafanaTemplate template,
            List<String> funcList) {
        return grafanaServiceExtension.requestGrafanaTemplate(group, title, area, template, funcList);
    }
    
    @Override
    public Map<String, String> beforeRequestGrafana(String area, String title) {
        return grafanaServiceExtension.beforeRequestGrafana(area, title);
    }
    
    @Override
    public String innerRequestGrafanaStr(String area, String title, String containerName, String group,
            GrafanaTemplate template, String application) {
        return grafanaServiceExtension.innerRequestGrafanaStr(area, title, containerName, group, template, application);
    }
    
    //Get grafana template variables
    @Override
    public Map<String, Object> getTemplateVariables(String folderId, String group, String title, String folderUid,
            String grafanaUrl, String containerName, String area, String application) {
        return grafanaServiceExtension.getTemplateVariables(folderId, group, title, folderUid, grafanaUrl,
                containerName, area, application);
    }
    
    //Replace the base panel and keep the user-defined panel
    @Override
    public String getFinalData(String data, String url, String apiKey, String method, String title,
            String panelIdList) {
        return grafanaServiceExtension.getFinalData(data, url, apiKey, method, title, panelIdList, false, null);
    }
    
    @Override
    public String innerRequestGrafana(String data, String url, String apiKey, String method) {
        return grafanaServiceExtension.innerRequestGrafana(data, url, apiKey, method);
    }
    
    @Override
    public void getCustomPanels(String grafanaStr, JsonArray basicPanels, int basicDiyPanelGirdPosY, String title,
            String panelIdList) {
        grafanaServiceExtension.getCustomPanels(grafanaStr, basicPanels, basicDiyPanelGirdPosY, title, panelIdList);
    }
    
    //Determine whether the request result of generating/updating the grafana graph is json in the specific format of grafana
    @Override
    public String isGrafanaDataJson(String jobJson) {
        return grafanaServiceExtension.isGrafanaDataJson(jobJson);
    }
    
    @Override
    public String getDashboardLastVersion(String dashboardId) {
        return grafanaServiceExtension.getDashboardLastVersion(dashboardId);
    }
}
