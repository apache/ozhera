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
package org.apache.ozhera.app.api.model;

import org.apache.ozhera.app.enums.OperateEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @version 1.0
 * @description
 * @date 2022/11/11 19:11
 */
@Data
public class HeraAppMqInfo implements Serializable {

    private OperateEnum operateEnum;

    private HeraAppBaseInfo beforeAppBaseInfo;

    private HeraAppBaseInfo afterAppBaseInfo;

    @Data
    public static class HeraAppBaseInfo {
        private Integer id;

        private String bindId;

        private Integer bindType;

        private String appName;

        private String appCname;

        private Integer appType;

        private String appLanguage;

        private Integer platformType;

        private String appSignId;

        private Integer iamTreeId;

        private Integer status;

        private Date createTime;

        private Date updateTime;

        private String envsMap;
    }
}
