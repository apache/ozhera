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
package run.mone.chaos.operator.controller;

import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import com.xiaomi.youpin.docean.anno.RequestParam;
import com.xiaomi.youpin.docean.mvc.MvcResult;
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.bo.restartPodListBo;
import run.mone.chaos.operator.service.ChaosBackendService;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zhangxiaowei6
 * @Date 2024/4/18 17:06
 * @Desc 用于提供后台接口
 */

@Controller
@Slf4j
public class ChaosBackendController {

    @Resource
    private ChaosBackendService backendService;

    // 用于recover失败,导致全局实验锁无法释放，提供解锁后台接口
    @RequestMapping(path = "/chaosBackend/deleteCacheKey", method = "get")
    public MvcResult<String> deleteCacheKey(@RequestParam(value = "id") String id) {
        return backendService.deleteCacheKey(id);
    }

    // 用于兜底，将传入的podIpList都进行重启
    @RequestMapping(path = "/chaosBackend/restartPodList")
    public MvcResult<String> restartPodList(restartPodListBo bo) {
        return backendService.restartPodList(bo);
    }

    //查询管理员
    @RequestMapping(path = "/chaosBackend/getAdmin", method = "get")
    public MvcResult<List<String>> getAdmin() {
        return backendService.getAdmin();
    }

    // 后端服务可用性
    @RequestMapping(path = "/chaosBackend/getServiceAvailabilityUrl", method = "get")
    public MvcResult<String> getServiceAvailabilityUrl() {

        return backendService.getServiceAvailabilityUrl();
    }

    @RequestMapping(path = "/chaosBackend/getReentrantLockValue", method = "get")
    public MvcResult<String> getReentrantLockValue(@RequestParam(value = "projectId") Integer projectId,@RequestParam(value = "pipelineId") Integer pipelineId) {
        return backendService.getReentrantLockValue(projectId,pipelineId);
    }
}
