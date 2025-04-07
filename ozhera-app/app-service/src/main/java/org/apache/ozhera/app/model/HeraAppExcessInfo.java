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
package org.apache.ozhera.app.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@TableName(value = "hera_app_excess_info", autoResultMap = true)

public class HeraAppExcessInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer appBaseId;

    @TableField(value = "tree_ids", typeHandler = JacksonTypeHandler.class)
    private List<Integer> treeIds;

    @TableField(value = "node_ips", typeHandler = JacksonTypeHandler.class)
    private LinkedHashMap<String, List<String>> nodeIPs;

    @TableField(value = "managers", typeHandler = JacksonTypeHandler.class)
    private List<String> managers;

    private Date createTime;

    private Date updateTime;
}
