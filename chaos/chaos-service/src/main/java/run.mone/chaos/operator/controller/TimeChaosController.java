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
import run.mone.chaos.operator.aspect.anno.ParamValidate;
import run.mone.chaos.operator.bo.TimeBO;
import run.mone.chaos.operator.service.TimeChaosService;

import javax.annotation.Resource;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-01-03 14:56
 */
@Controller
public class TimeChaosController {

    @Resource
    private TimeChaosService timeChaosService;

    @RequestMapping(path = "/timeChaos/setTimeOffset", method = "post")
    @ParamValidate
    public String setTimeOffset(TimeBO timeBO) {
        return timeChaosService.setTimeOffset(timeBO);
    }


    @RequestMapping(path = "/timeChaos/recoverTimeOffset", method = "post")
    public String recoverTimeOffset(TimeBO timeBO) {
        return timeChaosService.recoverTimeOffset(timeBO);
    }

}
