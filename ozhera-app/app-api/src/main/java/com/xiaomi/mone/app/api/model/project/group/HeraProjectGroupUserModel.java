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
package com.xiaomi.mone.app.api.model.project.group;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author gaoxihui
 * @date 2023/6/2 10:13 上午
 */
@Data
public class HeraProjectGroupUserModel implements Serializable {

    private Integer id;

    private Integer projectGroupId;

    private String user;

    private Integer status;

    private Date createTime;

    private Date updateTime;
}
