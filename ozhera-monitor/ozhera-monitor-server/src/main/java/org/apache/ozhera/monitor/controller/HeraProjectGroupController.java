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

import com.google.gson.Gson;
import org.apache.ozhera.app.api.model.HeraAppBaseInfoModel;
import org.apache.ozhera.app.api.model.project.group.HeraProjectGroupDataRequest;
import org.apache.ozhera.app.api.model.project.group.HeraProjectGroupModel;
import org.apache.ozhera.app.api.model.project.group.ProjectGroupTreeNode;
import org.apache.ozhera.app.common.Result;
import org.apache.ozhera.app.enums.CommonError;
import org.apache.ozhera.monitor.dao.model.AppMonitor;
import org.apache.ozhera.monitor.service.model.project.group.ProjectGroupRequest;
import org.apache.ozhera.monitor.service.project.group.ProjectGroupService;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2023/6/7 11:25 AM
 */
@Slf4j
@RestController
public class HeraProjectGroupController {

    @Autowired
    ProjectGroupService projectGroupService;

    @ResponseBody
    @PostMapping("/api/project-group/tree/full")
    public Result<ProjectGroupTreeNode> getFullTree(HttpServletRequest request, @RequestBody ProjectGroupRequest param) {

        Result result = projectGroupService.checkAuthorization(request);
        if(!result.isSuccess()){
            return result;
        }

        log.info("getFullTree param : {}", param);

        if(param.getGroupType() == null){
            log.error("getFullTree request param error! no group type found!");
            return Result.fail(CommonError.ParamsError);
        }

        return projectGroupService.getFullTree(param.getGroupType());
    }

    @ResponseBody
    @PostMapping("/api/project-group/tree")
    public Result<ProjectGroupTreeNode> getTreeByUser(HttpServletRequest request, @RequestBody ProjectGroupRequest param) {

        Result result = projectGroupService.checkAuthorization(request);
        if(!result.isSuccess()){
            return result;
        }

        if(StringUtils.isBlank(param.getUser())){
            log.error("getTreeByUser request param error! no group type found!");
            return Result.fail(CommonError.ParamsError);
        }

        log.info("getTreeByUser param : {}", param);

        if(param.getGroupType() == null){
            log.error("getTreeByUser request param error! no group type found!");
            return Result.fail(CommonError.ParamsError);
        }

        return projectGroupService.getTreeByUser(param);
    }

    @ResponseBody
    @PostMapping("/api/project-group/app")
    public Result<List<HeraAppBaseInfoModel>> getGroupApps(HttpServletRequest request, @RequestBody ProjectGroupRequest param) {

        Result result = projectGroupService.checkAuthorization(request);
        if(!result.isSuccess()){
            return result;
        }

        if(StringUtils.isBlank(param.getUser())){
            log.error("getGroupApps request param error! no group type found!");
            return Result.fail(CommonError.ParamsError);
        }

        log.info("getGroupApps param : {}", param);

        if(param.getGroupType() == null){
            log.error("getGroupApps request param error! no group type found!");
            return Result.fail(CommonError.ParamsError);
        }

        return projectGroupService.searchGroupApps(param);
    }

    @ResponseBody
    @PostMapping("/api/project-group/create")
    public Result createProjectGroup(HttpServletRequest request, @RequestBody HeraProjectGroupDataRequest param) {

        AuthUserVo userInfo = UserUtil.getUser();
        if (userInfo == null) {
            Result result = projectGroupService.checkAuthorization(request);
            if(!result.isSuccess()){
                return result;
            }
        }

        log.info("createProjectGroup param : {}", param);

        if(param.getType() == null || param.getRelationObjectId() == null || param.getParentGroupId() == null || StringUtils.isBlank(param.getName())){
            log.error("createProjectGroup request param error!param:{}",param);
            return Result.fail(CommonError.ParamsError);
        }

        Result<Integer> integerResult = projectGroupService.create(param);
        log.info("createProjectGroup param : {}, result : {}", param,new Gson().toJson(integerResult));

        return integerResult;
    }

    @ResponseBody
    @PostMapping("/api/project-group/update")
    public Result updateProjectGroup(HttpServletRequest request, @RequestBody HeraProjectGroupDataRequest param) {

        AuthUserVo userInfo = UserUtil.getUser();
        if (userInfo == null) {
            Result result = projectGroupService.checkAuthorization(request);
            if(!result.isSuccess()){
                return result;
            }
        }

        log.info("updateProjectGroup param : {}", param);

        if(param.getId() == null){
            log.error("updateProjectGroup request param error!param:{}",param);
            return Result.fail(CommonError.ParamsError);
        }

        return projectGroupService.update(param);
    }

    @ResponseBody
    @DeleteMapping("/api/project-group/delete/{id}")
    public Result deleteProjectGroup(HttpServletRequest request,@PathVariable("id") Integer id) {

        AuthUserVo userInfo = UserUtil.getUser();
        if (userInfo == null) {
            Result result = projectGroupService.checkAuthorization(request);
            if(!result.isSuccess()){
                return result;
            }
        }

        log.info("deleteProjectGroup id : {}", id);

        if(id == null){
            log.error("deleteProjectGroup request param error! id is null!");
            return Result.fail(CommonError.ParamsError);
        }

        return projectGroupService.delete(id);
    }


    @ResponseBody
    @PostMapping("/view/project-group/tree/full")
    public Result<ProjectGroupTreeNode> vieFullTree(HttpServletRequest request, @RequestBody ProjectGroupRequest param) {

        AuthUserVo userInfo = UserUtil.getUser();
        if (userInfo == null) {
            return Result.fail(CommonError.UNAUTHORIZED);
        }

        log.info("vieFullTree param : {}", param);

        if(param.getGroupType() == null){
            log.error("vieFullTree request param error! no group type found!");
            return Result.fail(CommonError.ParamsError);
        }

        return projectGroupService.getFullTree(param.getGroupType());
    }

    @ResponseBody
    @PostMapping("/view/project-group/tree")
    public Result<ProjectGroupTreeNode> viewTreeByUser(HttpServletRequest request, @RequestBody ProjectGroupRequest param) {

        AuthUserVo userInfo = UserUtil.getUser();
        if (userInfo == null) {
            return Result.fail(CommonError.UNAUTHORIZED);
        }
        log.info("viewTreeByUser param : {}", param);

        if(param.getGroupType() == null){
            log.error("getTreeByUser request param error! no group type found!");
            return Result.fail(CommonError.ParamsError);
        }

        param.setUser(userInfo.genFullAccount());

        return projectGroupService.getTreeByUser(param);
    }

    @ResponseBody
    @PostMapping("/view/project-group/apps")
    public Result<List<AppMonitor>> viewGroupApps(HttpServletRequest request, @RequestBody ProjectGroupRequest param) {

        AuthUserVo userInfo = UserUtil.getUser();
        if (userInfo == null) {
            return Result.fail(CommonError.UNAUTHORIZED);
        }

        String user = userInfo.genFullAccount();
        param.setUser(user);
        log.info("viewGroupApps param : {}", param);

        if(param.getGroupType() == null){
            log.error("viewGroupApps request param error! no group type found!");
            return Result.fail(CommonError.ParamsError);
        }
        return projectGroupService.searchMyApps(param);
    }

    @ResponseBody
    @PostMapping("/view/project-group/childs")
    public Result<List<HeraProjectGroupModel>> searchChildGroups(HttpServletRequest request, @RequestBody ProjectGroupRequest param) {

        AuthUserVo userInfo = UserUtil.getUser();
        if (userInfo == null) {
            return Result.fail(CommonError.UNAUTHORIZED);
        }

        String user = userInfo.genFullAccount();
        param.setUser(user);
        log.info("searchChildGroups param : {}", param);

        if(param.getGroupType() == null){
            log.error("searchChildGroups request param error! no group type found!");
            return Result.fail(CommonError.ParamsError);
        }

        if(param.getProjectGroupId() == null){
            log.error("searchChildGroups request param error! no projectGroupId found!");
            return Result.fail(CommonError.ParamsError);
        }
        return projectGroupService.searchChildGroups(param);
    }
}

