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
package com.xiaomi.mone.app.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/10/29 16:58
 */
public class Constant {

    public static final String SUCCESS_MESSAGE = "success";

    public static Gson GSON = new GsonBuilder().create();

    public static String DEFAULT_OPERATOR = "system";

    public static String DEFAULT_REGISTER_REMOTE_TYPE = "OZHERA_AUTO_COLLECT_ENVIP";


    public static class URL {
        // tpc query app url
        public static String HERA_TPC_APP_DETAIL_URL = "/backend/node/flag/inner_list";
        // operator query app ip url
        public static String HERA_OPERATOR_ENV_URL = "/envIps";
    }
}
