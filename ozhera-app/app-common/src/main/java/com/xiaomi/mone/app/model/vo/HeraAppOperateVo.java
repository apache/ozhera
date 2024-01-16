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

import com.xiaomi.mone.app.valid.AddGroup;
import com.xiaomi.mone.app.valid.UpdateGroup;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/10 10:09
 */
@Data
public class HeraAppOperateVo {

    @NotNull(message = "修改时候Id不能为空", groups = {UpdateGroup.class})
    @Null(message = "添加时候Id只能为空", groups = {AddGroup.class})
    private Long id;
    @NotNull
    private Long heraAppId;
    @NotNull
    private Long appId;
    @NotNull
    private String appName;
    @NotNull
    private Long envId;
    @NotNull
    private String envName;
    @NotEmpty
    private List<String> ipList;

    private String operator;

}
