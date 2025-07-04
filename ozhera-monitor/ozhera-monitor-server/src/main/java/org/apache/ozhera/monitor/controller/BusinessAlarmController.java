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

import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.monitor.bo.bizmetrics.BusinessAlarmRuleBo;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.BusinessAlarmService;
import org.apache.ozhera.monitor.service.model.PageData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @date 2025/4/23 4:38 下午
 */
@Slf4j
@Controller
public class BusinessAlarmController {


    @Autowired
    private BusinessAlarmService businessAlarmService;


    @ResponseBody
    @PostMapping("/business/metric/alarm/add")
    public Result<PageData> alarmAdd(@RequestBody BusinessAlarmRuleBo alarmRuleBo){

        log.info("BusinessAlarmController.alarmAdd request param : {} ",alarmRuleBo.toString());

        AuthUserVo userInfo = UserUtil.getUser();
        if(userInfo == null){
            log.info("BusinessAlarmController.alarmAdd request info error no user info found! ");
            return Result.fail(-1,"系统错误！");
        }

        String user = userInfo.genFullAccount();

        log.info("BusinessAlarmController.alarmAdd user : {}",user);

        return businessAlarmService.addRuleRemote(alarmRuleBo,user);

    }

    @ResponseBody
    @GetMapping("/business/metric/alarm/delete")
    public Result<Integer> alarmDel(Integer id){


        AuthUserVo userInfo = UserUtil.getUser();
        if(userInfo == null){
            log.info("BusinessAlarmController.alarmDel request info error no user info found! ");
            return Result.fail(-1,"系统错误！");
        }

        String user = userInfo.genFullAccount();

        log.info("BusinessAlarmController.alarmDel user : {}",user);

        return businessAlarmService.deleteRule(id, user);
    }
}
