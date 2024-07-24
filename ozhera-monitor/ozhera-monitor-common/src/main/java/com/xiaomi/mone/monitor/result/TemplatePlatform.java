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
package com.xiaomi.mone.monitor.result;
/**
 * @author zhangxiaowei6
 * @date 2022/3/30
 */
public enum TemplatePlatform {
    mione(0, "mione"),
    cloud(1, "cloud");
    private int code;
    private String message;

    TemplatePlatform(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static final TemplatePlatform getEnum(Integer code) {
        if (code == null) {
            return null;
        }
        for (TemplatePlatform userTypeEnum : TemplatePlatform.values()) {
            if (code.equals(userTypeEnum.code)) {
                return userTypeEnum;
            }
        }
        return null;
    }
}
