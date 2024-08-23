/*
 *  Copyright (C) 2020 Xiaomi Corporation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaomi.mone.monitor.service.user;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;

/**
 * @project: mimonitor
 * @author: zgf1
 * @date: 2022/1/25 14:11
 */
@Slf4j
public class LocalUser {

    private static final ThreadLocal<UseDetailInfo> local = new ThreadLocal<>();

    public static void set(UseDetailInfo user) {
        local.set(user);
    }

    public static final UseDetailInfo get() {
        return local.get();
    }

    public static void clear() {
        local.remove();
    }

    public static final Map<Integer, UseDetailInfo.DeptDescr> getDepts() {
       Map<Integer, UseDetailInfo.DeptDescr> map = Maps.newHashMap();
        UseDetailInfo user = LocalUser.get();
        log.info("debug_user_info={}", user);
        if (user == null || CollectionUtils.isEmpty(user.getFullDeptDescrList())) {
            return map;
        }
        for (UseDetailInfo.DeptDescr dept : user.getFullDeptDescrList()) {
            map.put(Integer.valueOf(dept.getLevel()), dept);
        }
       return map;
    }

}
