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

package org.apache.ozhera.monitor.service.user;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2022/1/18 4:23 下午
 */
@Service
public class UserConfigService {
    @NacosValue(value = "${hera.admin.member.list}", autoRefreshed = true)
    private String adminMemberList;

    @NacosValue(autoRefreshed = true,value = "${assign.login.user:}")
    private String assignLoginUser;

    private  String superAdmin = "gaoxihui";

    public List<String> getAdminUserList() {
        if(StringUtils.isBlank(adminMemberList)){
            return Lists.newArrayList();
        }
        String[] admins = adminMemberList.split(",");

        return Arrays.asList(admins);
    }

    public boolean isAdmin(String user){

        return getAdminUserList().contains(user) ? true : false;
    }

    public String getAssignUser(String currentUser){

        if(!isSuperAdmin(currentUser)){
            return currentUser;
        }

        if(StringUtils.isBlank(assignLoginUser)){
            return currentUser;
        }

        return assignLoginUser;

    }

    public boolean isSuperAdmin(String user){
        if(StringUtils.isBlank(user)){
            return false;
        }
        return user.equals(superAdmin) ? true : false;
    }
}
