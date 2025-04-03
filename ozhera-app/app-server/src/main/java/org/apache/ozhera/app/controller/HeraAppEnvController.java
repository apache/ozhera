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
package org.apache.ozhera.app.controller;

import org.apache.ozhera.app.model.vo.HeraAppEnvVo;
import org.apache.ozhera.app.model.vo.HeraAppOperateVo;
import org.apache.ozhera.app.service.HeraAppEnvService;
import org.apache.ozhera.app.valid.AddGroup;
import org.apache.ozhera.app.valid.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/hera/app/env")
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class HeraAppEnvController {

    @Autowired
    private HeraAppEnvService heraAppEnvService;

    @GetMapping("/id")
    public HeraAppEnvVo queryAppEnvById(@RequestParam("id") Long id) {
        return heraAppEnvService.queryAppEnvById(id);
    }

    @PostMapping("/add")
    public Long addAppEnv(@Validated({AddGroup.class}) @RequestBody HeraAppOperateVo operateVo) {
        return heraAppEnvService.addAppEnv(operateVo);
    }

    @PostMapping("/update")
    public Long updateAppEnv(@Validated({UpdateGroup.class}) @RequestBody HeraAppOperateVo operateVo) {
        return heraAppEnvService.updateAppEnv(operateVo);
    }

    @PostMapping("/delete")
    public Boolean deleteAppEnv(@RequestParam("id") Long id) {
        return heraAppEnvService.deleteAppEnv(id);
    }

    /**
     * query the IP information of non probe connected application machines
     *
     * @return
     */
    @GetMapping("/non/probe/ips")
    public List<String> queryNonProbeAccessIPs() {
        return heraAppEnvService.queryNonProbeAccessIPs();
    }
}
