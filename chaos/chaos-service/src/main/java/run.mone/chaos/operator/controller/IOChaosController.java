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
import run.mone.chaos.operator.bo.IOBO;
import run.mone.chaos.operator.constant.io.IOMethodEnum;
import run.mone.chaos.operator.constant.io.IOTypeEnum;
import run.mone.chaos.operator.service.IoChaosService;

import javax.annotation.Resource;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/12 19:10
 */
@Slf4j
@Controller
//可能会损坏你的数据，在生产环境中请谨慎使用!
//注意：必须是挂载卷的路径才可以生效
public class IOChaosController {

    @Resource
    IoChaosService ioChaosService;

    //为文件系统调用加入延迟、使文件系统调用返回错误、修改文件属性
    @ParamValidate
    @ChaosAfter
    @RequestMapping(path = "/ioChaos/injectLatency", method = "post")
    public String latency(IOBO ioBO) {
        log.info("IoChaosController.latency param:{}", ioBO);
        String checkRes = latencyArgCheck(ioBO, false);
        if (!"ok".equals(checkRes)) {
            return checkRes;
        }
        return ioChaosService.latency(ioBO);
    }

    @ParamValidate
    @ChaosAfter
    @RequestMapping(path = "/ioChaos/injectLatencyRecover", method = "post")
    public String latencyRecover(IOBO ioBO) {
        log.info("IoChaosController.latency param:{}", ioBO);
        String checkRes = latencyArgCheck(ioBO, true);
        if (!"ok".equals(checkRes)) {
            return checkRes;
        }
        return ioChaosService.latencyRecover(ioBO);
    }


    private String latencyArgCheck(IOBO ioBO, boolean isRecover) {
        if (isRecover) {
            if (ioBO == null || ioBO.getPipelineId() == null || ioBO.getProjectId() == null) {
                return "参数错误";
            } else {
                return "ok";
            }
        } else {
            if (ioBO == null || ioBO.getPipelineId() == null || ioBO.getProjectId() == null || ioBO.getPath() == null ||
                    ioBO.getPercent() == null || ioBO.getLatency() == null || ioBO.getMethods() == null) {
                return "参数错误";
            }
            //ioBO.getPercent()必须在1~100之间
            if (ioBO.getPercent() > 100 || ioBO.getPercent() < 1) {
                return "百分比错误";
            }
            // type类型校验
            if (!IOTypeEnum.IsContainByTypeName(ioBO.getType())) {
                return "IO 故障类型错误";
            }
            AtomicBoolean isMethodRight = new AtomicBoolean(true);
            ioBO.getMethods().forEach(method -> {
                //1个不支持就返回
                if (!IOMethodEnum.IsContainByTypeName(method)) {
                    isMethodRight.set(false);
                }
            });
            if (!isMethodRight.get()) {
                return "方法不支持";
            }
            if (!ioBO.getPath().startsWith(ioBO.getVolume())) {
                return "目录不匹配";
            }
            return "ok";
        }
    }

}
