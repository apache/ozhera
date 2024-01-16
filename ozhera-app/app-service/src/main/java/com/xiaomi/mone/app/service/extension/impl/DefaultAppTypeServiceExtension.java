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
package com.xiaomi.mone.app.service.extension.impl;

import com.xiaomi.mone.app.enums.PlatFormTypeEnum;
import com.xiaomi.mone.app.enums.ProjectTypeEnum;
import com.xiaomi.mone.app.service.extension.AppTypeServiceExtension;
import com.xiaomi.mone.app.util.AppTypeTransferUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/4/24 17:03
 */
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class DefaultAppTypeServiceExtension implements AppTypeServiceExtension {
    @Override
    public Integer getAppTypeLog(Integer type) {
        return null;
    }

    @Override
    public Integer getAppPlatForm(Integer type) {
        return AppTypeTransferUtil.queryPlatformTypeWithLogType(type);
    }

    @Override
    public Integer getAppTypePlatformType(Integer type) {
        return AppTypeTransferUtil.queryLogTypeWithPlatformType(type);
    }

    @Override
    public String getPlatformName(Integer platformType) {
        return PlatFormTypeEnum.getEnum(platformType).getName();
    }

    @Override
    public String getAppTypeName(Integer appType) {
        return ProjectTypeEnum.queryTypeByCode(appType);
    }
}
