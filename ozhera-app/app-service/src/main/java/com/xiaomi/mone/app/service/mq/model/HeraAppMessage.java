/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.mone.app.service.mq.model;

import com.google.gson.JsonObject;
import com.xiaomi.mone.app.model.HeraAppBaseInfo;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author gaoxihui
 * @date 2022/2/21 2:38 下午
 */

@ToString
@Data
public class HeraAppMessage implements Serializable {

    private String id;

    private Integer iamTreeId;

    private Integer iamTreeType;

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

    private Map<String,String> env;

    public HeraAppBaseInfo baseInfo(){

        HeraAppBaseInfo heraAppBaseInfo = new HeraAppBaseInfo();

        heraAppBaseInfo.setBindId(String.valueOf(this.getId()));
        heraAppBaseInfo.setBindType(this.getBindType());
        heraAppBaseInfo.setAppName(this.getAppName());
        heraAppBaseInfo.setAppCname(this.getAppCname());

        if(env != null && StringUtils.isNotBlank(env.get("appLanguage"))){
            heraAppBaseInfo.setAppLanguage(env.get("appLanguage"));
        }else{
            heraAppBaseInfo.setAppLanguage(this.getAppLanguage());
        }
        heraAppBaseInfo.setPlatformType(this.getPlatformType());
        heraAppBaseInfo.setAppType(this.getAppType());
        heraAppBaseInfo.setEnvsMap(this.getEnvMapping() == null ? "" : this.getEnvMapping().toString());

        heraAppBaseInfo.setIamTreeId(this.getIamTreeId());
        heraAppBaseInfo.setIamTreeType(this.getIamTreeType());

        return heraAppBaseInfo;
    }

}
