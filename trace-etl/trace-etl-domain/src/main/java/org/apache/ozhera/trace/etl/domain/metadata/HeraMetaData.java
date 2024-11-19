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

package org.apache.ozhera.trace.etl.domain.metadata;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @Author dingtao
 */
@Data
@TableName(value = "hera_meta_data", autoResultMap = true)
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HeraMetaData {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Metadata id, such as appId
     */
    private Integer metaId;

    /**
     * The name of the metadata, the app type is appName, the mysql type is DBName defined by the DBA, and so on
     */
    private String metaName;

    private Integer envId;

    private String envName;

    /**
     * dubbo service metadata, group/service/version, separated by commas
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
    @TableField(value = "port", typeHandler = JacksonTypeHandler.class)
    private HeraMetaDataPort port;

    private Date createTime;

    private Date updateTime;

    private String createBy;

    private String updateBy;

}