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
package org.apache.ozhera.app.enums;

import lombok.Getter;

/**
 * @version 1.0
 * @description milog对应的应用类型
 * @date 2021/10/14 10:28
 */
@Getter
public enum ProjectTypeEnum {

    MIONE_TYPE(0, "mione", PlatFormTypeEnum.CHINA.getCode());

    private final Integer code;
    private final String type;
    private final Integer platFormTypeCode;

    ProjectTypeEnum(Integer code, String type, Integer platFormTypeCode) {
        this.code = code;
        this.type = type;
        this.platFormTypeCode = platFormTypeCode;
    }

    public static String queryTypeByCode(Integer code) {
        for (ProjectTypeEnum value : ProjectTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value.getType();
            }
        }
        return "";
    }
}
