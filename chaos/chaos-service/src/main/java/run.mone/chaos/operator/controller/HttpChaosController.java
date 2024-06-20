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
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.aspect.anno.ChaosAfter;
import run.mone.chaos.operator.aspect.anno.ParamValidate;
import run.mone.chaos.operator.bo.http.HttpBO;
import run.mone.chaos.operator.service.HttpChaosService;

import javax.annotation.Resource;

/**
 * @author caobaoyu
 * @description: http chaos
 * @date 2023-12-04 10:25
 */
@Slf4j
@Controller
/**
 * 注意：
 * 1、HTTPChaos 暂不支持注入 HTTPS 连接
 * 2、实验运行时注入故障的优先级（顺序）固定为 abort -> delay -> replace -> patch。其中 abort 故障会导致短路，直接中断此次连接
 * 3、在生产环境下谨慎使用非幂等语义请求（例如大多数 POST 请求）。若使用了这类请求，注入故障后可能无法通过重复请求使目标服务恢复正常状态
 */
public class HttpChaosController {

    @Resource
    private HttpChaosService httpChaosService;

    @RequestMapping(path = "/httpChaos/recover", method = "post")
    @ParamValidate
    @ChaosAfter
    public String recover(HttpBO httpBO) {
        log.info("injectHttpResponsePatch httpBO:{}", httpBO);
        String checkRes = commonHttpParamCheck(httpBO, true);
        if (!"ok".equals(checkRes)) {
            return checkRes;
        }
        return httpChaosService.httpRecover(httpBO);
    }

    //abort 中断服务端的连接
    //delay 为目标过程注入延迟
    //replace 替换请求报文或者响应报文的部分内容
    //patch 给请求报文或响应报文添加额外内容
    @RequestMapping(path = "/httpChaos/injectHttpChaos", method = "post")
    @ParamValidate
    @ChaosAfter
    public String injectHttpChaos(HttpBO httpBO) {
        log.info("injectHttpRequestDelay httpBO:{}", httpBO);
        String checkRes = commonHttpParamCheck(httpBO, false);
        if (!"ok".equals(checkRes)) {
            return checkRes;
        }
        return httpChaosService.httpDo(httpBO);
    }

    private String commonHttpParamCheck(HttpBO httpBO, boolean isRecover) {
        if (isRecover) {
            //必须有rules且为空数组
            if (httpBO.getRules() == null || !httpBO.getRules().isEmpty()) {
                return "恢复操作必须为空操作!";
            }
        } else {
            //必须有proxy_ports、rules
            if (httpBO.getProxy_ports() == null || httpBO.getProxy_ports().isEmpty()) {
                return "故障参数非法!";
            }
        }
        return "ok";
    }

}
