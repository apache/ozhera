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
package org.apache.ozhera.monitor.bo;

import org.apache.commons.lang3.StringUtils;

/**
 * @author zhangxiaowei6
 * @date 2022/3/30
 */
public enum AppLanguage {
    java(0, "java"),
    go(1, "golang"),
    python(2,"python"),
    php(3,"php");
    private int code;
    private String message;

    AppLanguage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static final AppLanguage getEnum(Integer code) {
        if (code == null) {
            return null;
        }
        for (AppLanguage userTypeEnum : AppLanguage.values()) {
            if (code.equals(userTypeEnum.code)) {
                return userTypeEnum;
            }
        }
        return null;
    }

    public static final Integer getCodeByMessage(String msg) {
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        for (AppLanguage userTypeEnum : AppLanguage.values()) {
            if (msg.equals(userTypeEnum.getMessage())) {
                return userTypeEnum.getCode();
            }
        }
        return null;
    }
}
