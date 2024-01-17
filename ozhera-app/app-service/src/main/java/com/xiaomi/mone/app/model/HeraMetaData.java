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
package com.xiaomi.mone.app.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.xiaomi.mone.app.api.model.HeraMetaDataType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/4/28 10:04 AM
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
     * 元数据id，比如appId
     */
    private Integer metaId;

    /**
     * 元数据的名称，app类型就是appName，mysql类型就是DBA定义的DBName等等
     */
    private String metaName;

    private Integer envId;

    private String envName;

    /**
     * dubbo service 元数据，group/service/version，多个以逗号分隔
     */
    private String dubboServiceMeta;

    /**
     * 元数据类型，有APP、MYSQL、REDIS、ES、MQ等，具体可以参照{@link HeraMetaDataType}
     */
    private String type;

    /**
     *元数据的实例，有可能是IP，有可能是域名，也有可能是hostName
     */
    private String host;

    /**
     *该元数据暴露的端口
     */
    @TableField(value = "port", typeHandler = JacksonTypeHandler.class)
    private HeraMetaDataPort port;

    private Date createTime;

    private Date updateTime;

    private String createBy;

    private String updateBy;

}
