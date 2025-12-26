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
package org.apache.ozhera.app.service;

import org.apache.ozhera.app.api.model.HeraAppRoleModel;
import org.apache.ozhera.app.dao.HeraAppRoleDao;
import org.apache.ozhera.app.model.HeraAppRole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @date 2022/11/22 7:49 PM
 */
@Slf4j
@Service
public class HeraAppRoleService {

    @Autowired
    HeraAppRoleDao dao;

    public Integer delById(Long id){
        return dao.delById(id);
    }

    public Integer addRoleGet(String appId, Integer plat, String user){
        HeraAppRoleModel role = new HeraAppRoleModel();
        role.setAppId(appId);
        role.setAppPlatform(plat);
        role.setUser(user);
        role.setStatus(1);
        role.setRole(0);
        role.setCreateTime(new Date());
        role.setUpdateTime(new Date());
        return addRole(role);
    }

    public Integer addRole(HeraAppRoleModel roleModel){

        HeraAppRole role = new HeraAppRole();
        BeanUtils.copyProperties(roleModel,role);
        return dao.create(role);
    }

    public List<HeraAppRoleModel> query(HeraAppRoleModel roleModel,Integer pageCount,Integer pageNum){

        HeraAppRole role = new HeraAppRole();
        BeanUtils.copyProperties(roleModel,role);
        List<HeraAppRole> query = dao.query(role, pageCount, pageNum);

        List<HeraAppRoleModel> result = new ArrayList<>();
        if(!CollectionUtils.isEmpty(query)){
            query.stream().forEach(t ->{
                HeraAppRoleModel model = new HeraAppRoleModel();
                BeanUtils.copyProperties(t,model);
                result.add(model);
            });
        }

        return result;

    }

    public Long count(HeraAppRoleModel roleModel){
        HeraAppRole role = new HeraAppRole();
        BeanUtils.copyProperties(roleModel,role);
        Long count = dao.count(role);
        return count;
    }

}
