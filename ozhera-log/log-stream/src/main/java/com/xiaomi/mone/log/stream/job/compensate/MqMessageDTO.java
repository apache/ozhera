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
package com.xiaomi.mone.log.stream.job.compensate;

import com.xiaomi.mone.log.model.StorageInfo;
import lombok.Data;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/1/17 10:57
 */
@Data
public class MqMessageDTO {

    private StorageInfo esInfo;

    private List<CompensateMqDTO> compensateMqDTOS;

    @Data
    public static class CompensateMqDTO {
        private String esIndex;
        private String msg;
    }

}
