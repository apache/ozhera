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
package com.xiaomi.mone.app.enums;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/10/29 13:22
 */
public enum CommonError {
    Success(0, "success"),

    UnknownError(-1, "unknown error"),

    ParamsError(400, "parameter error"),

    UNAUTHORIZED(401, "身份未认证"),

    NOT_EXISTS_DATA(204, "数据不存在"),

    NO_AUTHORIZATION(403, "没有权限"),

    SERVER_ERROR(500, "服务器异常"),

    NO_TOKEN(1001, "no token info"),
    INVALID_TOKEN(1002, "token is invalid or expired"),

    ;

    private int code;
    private String message;

    CommonError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
