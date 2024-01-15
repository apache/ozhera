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
package com.xiaomi.mone.log.manager.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/10 16:02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogStorageData {

    private Long clusterId;

    private Long storeId;

    private String logStoreName;

    private String updateStoreName;

    private String keys;

    private Integer logType;

    private String columnTypes;

    private String updateKeys;

    private String updateColumnTypes;
}
