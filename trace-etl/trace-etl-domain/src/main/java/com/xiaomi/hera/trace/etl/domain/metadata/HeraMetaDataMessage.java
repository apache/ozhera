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
package com.xiaomi.hera.trace.etl.domain.metadata;

import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @Author dingtao
 */
@Data
@ToString
public class HeraMetaDataMessage {
    private Integer metaId;

    private String metaName;

    private Integer envId;

    private String envName;

    private String type;

    private String host;

    private HeraMetaDataPortModel port;

    /**
     * insert、update
     */
    private String operator;

    private Date createTime;

    private Date updateTime;

    private String createBy;

    private String updateBy;
}
