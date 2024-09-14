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
package org.apache.ozhera.trace.etl.domain.metadata;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author dingtao
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HeraMetaDataModel implements Serializable {

    private Long id;

    private Integer metaId;

    /**
     * The name of the metadata, the app type is appName, the mysql type is DBName defined by the DBA, and so on
     */
    private String metaName;

    private Integer envId;

    private String envName;

    /**
     * dubbo service metadata，group/service/version, Multiple are separated by commas
     */
    private String dubboServiceMeta;

    /**
     * Metadata types include APP, MYSQL, REDIS, ES, MQ, etc., for details, see{@link HeraMetaDataType}
     */
    private String type;

    /**
     * The instance of metadata may be IP, domain name, or hostName
     */
    private String host;

    /**
     * The port exposed by the metadata
     */
    private HeraMetaDataPortModel port;

    private Date createTime;

    private Date updateTime;

    private String createBy;

    private String updateBy;
}
