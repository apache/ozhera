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

package org.apache.ozhera.monitor.controller;

import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.model.UserInfo;
import org.apache.ozhera.monitor.service.user.LocalUser;
import org.apache.ozhera.monitor.service.user.UseDetailInfo;
import org.apache.ozhera.monitor.service.user.UserConfigService;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author gaoxihui
 * @date 2021/9/9 10:10 AM
 */
@Slf4j
@RestController
public class UserController {


    @Autowired
    UserConfigService userConfigService;

    @ResponseBody
    @RequestMapping("/user/info")
    public Result userInfo(HttpServletRequest request){

        try {
            AuthUserVo userVo = UserUtil.getUser();
            if(userVo == null){
                log.info("UserController.userInfo request info error no user info found!");
                return Result.fail(ErrorCode.unknownError);
            }

            UserInfo userInfo = new UserInfo();
            userInfo.setDepartmentName(userVo.getDepartmentName());
            userInfo.setDisplayName(userVo.getName());
            userInfo.setEmail(userVo.getEmail());
            userInfo.setName(userVo.getName());
            userInfo.setUser(userVo.genFullAccount());
            userInfo.setAvatar(userVo.getAvatarUrl());
            userInfo.setIsAdmin(userConfigService.isAdmin(userVo.genFullAccount()) ? true : false);

            Map<Integer, UseDetailInfo.DeptDescr> depts = LocalUser.getDepts();
            UseDetailInfo.DeptDescr dept = depts == null ? null : depts.get(1);
            UseDetailInfo.DeptDescr dept2 = depts == null ? null : depts.get(2);
            userInfo.setFirstDepartment(dept == null ? null : dept.getDeptName());
            userInfo.setSecondDepartment(dept2 == null ? null : dept2.getDeptName());

            log.info("UserController.userInfo userInfo :{}",userInfo);
            return Result.success(userInfo);
        } catch (Exception e) {
            log.error("UserController.addAlarmTemplate",e);
            return Result.fail(ErrorCode.unknownError);
        }
    }

}
