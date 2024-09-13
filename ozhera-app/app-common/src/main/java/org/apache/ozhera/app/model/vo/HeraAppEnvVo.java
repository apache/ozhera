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
package org.apache.ozhera.app.model.vo;

import org.apache.ozhera.app.model.BaseCommon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/9 17:51
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeraAppEnvVo extends BaseCommon {

    private Long id;

    private Long heraAppId;

    private Long appId;

    private String appName;

    private List<EnvVo> envVos;


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EnvVo {
        private Long envId;

        private String envName;

        private List<String> ipList;
    }
}