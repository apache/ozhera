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
package org.apache.ozhera.app.util;

import org.apache.ozhera.app.enums.PlatFormTypeEnum;
import org.apache.ozhera.app.enums.ProjectTypeEnum;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * @version 1.0
 * @description
 * @date 2022/10/29 15:50
 */
public class AppTypeTransferUtil {
    private AppTypeTransferUtil() {
    }

    /**
     * 通过milog的应用类型查找对应的应用类型
     *
     * @param logTypeCode
     * @return
     */
    public static Integer queryPlatformTypeWithLogType(Integer logTypeCode) {
        Optional<ProjectTypeEnum> typeEnumOptional = Arrays.stream(ProjectTypeEnum.values())
                .filter(projectTypeEnum -> Objects.equals(logTypeCode, projectTypeEnum.getCode()))
                .findFirst();
        if (typeEnumOptional.isPresent()) {
            return typeEnumOptional.get().getPlatFormTypeCode();
        }
        return null;
    }

    /**
     * 通过milog的应用类型查找对应的应用类型
     *
     * @param logTypeCode
     * @return
     */
    public static Integer queryLogTypeWithPlatformType(Integer logTypeCode) {
        Optional<PlatFormTypeEnum> typeEnumOptional = Arrays.stream(PlatFormTypeEnum.values())
                .filter(projectTypeEnum -> Objects.equals(logTypeCode, projectTypeEnum.getCode()))
                .findFirst();
        if (typeEnumOptional.isPresent()) {
            return typeEnumOptional.get().getProjectTypeCode();
        }
        return null;
    }
}
