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
package com.xiaomi.mone.app.api.message;

import com.google.gson.JsonObject;
import com.xiaomi.mone.app.api.model.HeraAppBaseInfoModel;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2023/4/26 3:29 下午
 */
@Data
@ToString
public class HeraAppInfoModifyMessage implements Serializable {

        private Integer id;

        private Integer appId;

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

        private Boolean isNameChange;

        private Boolean isPlatChange;

        private Boolean isIamTreeIdChange;

        private Boolean isIamTreeTypeChange;

        private HeraAppModifyType modifyType;

        public HeraAppBaseInfoModel baseInfoModel(){
                HeraAppBaseInfoModel model = new HeraAppBaseInfoModel();
                model.setId(this.getId());
                model.setBindId(String.valueOf(this.getAppId()));
                model.setBindType(this.getBindType());
                model.setAppName(this.getAppName());
                model.setAppCname(this.getAppCname());
                model.setAppType(this.getAppType());
                model.setAppLanguage(this.getAppLanguage());
                model.setPlatformType(this.getPlatformType());
                model.setIamTreeId(this.getIamTreeId());
                model.setIamTreeType(this.getIamTreeType());
                model.setEnvsMap(this.getEnvMapping() != null ? this.getEnvMapping().toString() : null);
                return model;
        }

}
