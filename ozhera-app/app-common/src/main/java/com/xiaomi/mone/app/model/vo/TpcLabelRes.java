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
package com.xiaomi.mone.app.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wtt
 * @version 1.0
 * @description tpc标签页返回值
 * @date 2023/8/16 16:35
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TpcLabelRes {
    private Long id;
    private Integer type;
    private Integer status;
    private String desc;
    private String content;
    private Long createrId;
    private String createrAcc;
    private Integer createrType;
    private Long updaterId;
    private String updaterAcc;
    private Integer updaterType;
    private Long createTime;
    private Long updateTime;
    private Integer parentId;
    private String flagName;
    private String flagKey;
    private String flagVal;
    private Long parentIdV2;

}
