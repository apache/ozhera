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
package com.xiaomi.mone.log.stream.plugin.nacos;

import lombok.Data;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/27 10:24
 */
@Data
public class LogFilterConfig {

    private Long tailId;

    private boolean enableFilter;

    private List<LogFieldFilter> logFieldFilterList;

    @Data
    public static class LogFieldFilter {
        private String logField;
        private String filterKeyWord;
    }
}
