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

package org.apache.ozhera.monitor.service.rocketmq.model;

import com.google.gson.JsonObject;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.monitor.dao.model.AppMonitor;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2022/2/21 2:38 PM
 */

@ToString
@Data
public class HeraAppMessage implements Serializable {

    private String id;

    private Integer iamTreeId;

    private String appName;

    private String appCname;

    private String owner;

    private Integer platformType;

    private Integer bindType;

    private String appLanguage;

    private Integer appType;

    private JsonObject envMapping;

    private List<String> joinedMembers;

    private Integer delete;

    public AppMonitor appMonitor(){
        AppMonitor appMonitor = new AppMonitor();
        BeanUtils.copyProperties(this,appMonitor);
        return appMonitor;
    }

    public HeraAppBaseInfoModel baseInfo(){

        HeraAppBaseInfoModel heraAppBaseInfo = new HeraAppBaseInfoModel();

        heraAppBaseInfo.setBindId(String.valueOf(this.getId()));
        heraAppBaseInfo.setBindType(this.getBindType());
        heraAppBaseInfo.setAppName(this.getAppName());
        heraAppBaseInfo.setAppCname(this.getAppCname());

        heraAppBaseInfo.setAppLanguage(this.getAppLanguage());
        heraAppBaseInfo.setPlatformType(this.getPlatformType());
        heraAppBaseInfo.setAppType(this.getAppType());
        heraAppBaseInfo.setEnvsMap(this.getEnvMapping() == null ? "" : this.getEnvMapping().toString());

        heraAppBaseInfo.setIamTreeId(this.getIamTreeId());

        return heraAppBaseInfo;
    }

}
