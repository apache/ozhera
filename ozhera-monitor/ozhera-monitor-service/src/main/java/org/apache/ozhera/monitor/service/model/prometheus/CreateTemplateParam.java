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

package org.apache.ozhera.monitor.service.model.prometheus;

import org.apache.ozhera.monitor.bo.AppLanguage;
import org.apache.ozhera.monitor.bo.AppType;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

/**
 * @author zhangxiaowei6
 * @date 2022/3/30
 * */
@Data
@ToString(callSuper = true)
public class CreateTemplateParam  implements Serializable {
    private String name;
    private String template;
    private Integer platform;
    private Integer language;
    private Integer appType;
    private String urlParam;
    private Long id;
    private String panelIdList;

    public boolean check() {
        if (AppLanguage.getEnum(language) == null) {
            return false;
        }
        if (AppType.getEnum(appType) == null) {
            return false;
        }
        if (StringUtils.isBlank(name)) {
            return false;
        }
        if (StringUtils.isBlank(panelIdList)) {
            return false;
        }
        if (StringUtils.isBlank(template)) {
            return false;
        }
        return true;
    }

}
