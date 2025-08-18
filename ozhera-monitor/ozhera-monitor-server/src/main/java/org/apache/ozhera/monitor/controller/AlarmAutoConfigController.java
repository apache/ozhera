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

import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.AppMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author gaoxihui
 * @date 2021/9/9 10:10 AM
 */
@Slf4j
@RestController
public class AlarmAutoConfigController {

    @Autowired
    AppMonitorService appMonitorService;

    @ResponseBody
    @GetMapping("/manual/alarm/appPlatMove")
    public Result appPlatMove(Integer OProjectId,Integer OPlat,Integer NProjectId,Integer Nplat,Integer newIamId,String NprojectName){

        appMonitorService.appPlatMove(OProjectId, OPlat, NProjectId, Nplat, newIamId, NprojectName,true);

        return Result.success();
    }



}
