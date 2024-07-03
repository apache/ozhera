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
package run.mone.chaos.operator.dao.domain;

import dev.morphia.annotations.Id;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/13 19:03
 */

@Data
public abstract class BaseDomain implements Serializable {
    @Id
    private ObjectId id;
    private String createUser;

    private String updateUser;

    private Long createTime;

    private Long updateTime;

    private Long endTime;

    private int deleted;

    public void insertInit() {
        if (createTime == null) {
            createTime = System.currentTimeMillis();
        }
        if (updateTime == null) {
            updateTime = System.currentTimeMillis();
        }

        if (createUser == null) {
            createUser = "mione";
        }
        if (updateUser == null) {
            updateUser = "mione";
        }
    }

    public void updateInit() {
        updateTime = System.currentTimeMillis();
        if (updateUser == null) {
            updateUser = "unknown";
        }
    }
}
