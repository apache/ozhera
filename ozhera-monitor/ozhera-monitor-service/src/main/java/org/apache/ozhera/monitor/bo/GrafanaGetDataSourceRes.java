/*
 * Copyright (C) 2020 Xiaomi Corporation
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
package org.apache.ozhera.monitor.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author zhangxiaowei6
 * @date 2023-03-21
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GrafanaGetDataSourceRes {
    private int id;
    private String uid;
    private int orgId;
    private String name;
    private String type;
    private String typeLogoUrl;
    private String access;
    private String url;
    private String password;
    private String user;
    private String database;
    private boolean basicAuth;
    private String basicAuthUser;
    private String basicAuthPassword;
    private boolean withCredentials;
    private boolean isDefault;
    private int version;
    private boolean readOnly;
    private String message;
}