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
package org.apache.ozhera.app.aop;

import org.apache.ozhera.app.api.model.HeraAppMqInfo;
import org.apache.ozhera.app.common.Result;
import org.apache.ozhera.app.enums.OperateEnum;
import org.apache.ozhera.app.model.HeraAppBaseInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.apache.ozhera.app.common.Constant.GSON;

/**
 * @version 1.0
 * @description
 * @date 2022/11/11 18:34
 */
//@Configuration
//@Aspect
@Slf4j
public class OperateAppBaseInfoAspect {

    private final String appGetResultDataPoint = "execution(* org.apache.ozhera.app.service.impl.HeraAppBaseInfoServiceImpl.appBaseInfoOperate(..))";

    @Value("${rocket.mq.hera.app.topic}")
    private String topic;

    @Value("${rocket.mq.hera.app.tag}")
    private String tag;

    @Autowired
    private DefaultMQProducer defaultMQProducer;

    @AfterReturning(value = appGetResultDataPoint, returning = "result")
    public void afterOperateAppBaseInfo(JoinPoint joinPoint, Object result) throws IOException {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        try {
            // 获得切点方法的参数
            Object[] joinPointArgs = joinPoint.getArgs();

            HeraAppBaseInfo parameterAppBaseInfo = (HeraAppBaseInfo) joinPointArgs[0];
            HeraAppBaseInfo resultAppBaseInfo = (HeraAppBaseInfo) result;

            OperateEnum operateEnum = (OperateEnum) joinPointArgs[1];
            String mqMessage = GSON.toJson(Result.success(assemblyAppMqInfo(parameterAppBaseInfo, resultAppBaseInfo, operateEnum)));
            log.info("mq data:{}", mqMessage);

            Message message = new Message();
            message.setTopic(this.topic);
            message.setTags(this.tag);
            message.setBody(mqMessage.getBytes(StandardCharsets.UTF_8));
            defaultMQProducer.send(message);
            //发送mq消息
            log.info("发送mq消息成功");
        } catch (Throwable e) {
            log.error("hear app operate send mq error", e);
        }

    }

    private HeraAppMqInfo assemblyAppMqInfo(HeraAppBaseInfo parameterAppBaseInfo, HeraAppBaseInfo resultAppBaseInfo,
                                            OperateEnum operateEnum) {
        HeraAppMqInfo heraAppMqInfo = new HeraAppMqInfo();
        heraAppMqInfo.setOperateEnum(operateEnum);
        if (OperateEnum.ADD_OPERATE == operateEnum) {
            parameterAppBaseInfo.setId(null);
            heraAppMqInfo.setBeforeAppBaseInfo(appBaseInfoTransferMqBaseInfo(parameterAppBaseInfo));
            heraAppMqInfo.setAfterAppBaseInfo(appBaseInfoTransferMqBaseInfo(resultAppBaseInfo));
        }
        if (OperateEnum.UPDATE_OPERATE == operateEnum) {
            heraAppMqInfo.setBeforeAppBaseInfo(appBaseInfoTransferMqBaseInfo(resultAppBaseInfo));
            heraAppMqInfo.setAfterAppBaseInfo(appBaseInfoTransferMqBaseInfo(parameterAppBaseInfo));
        }
        if (OperateEnum.DELETE_OPERATE == operateEnum) {
            heraAppMqInfo.setBeforeAppBaseInfo(appBaseInfoTransferMqBaseInfo(parameterAppBaseInfo));
        }
        return heraAppMqInfo;
    }

    private HeraAppMqInfo.HeraAppBaseInfo appBaseInfoTransferMqBaseInfo(HeraAppBaseInfo heraAppBaseInfo) {
        HeraAppMqInfo.HeraAppBaseInfo mqAppBaseInfo = new HeraAppMqInfo.HeraAppBaseInfo();
        BeanUtils.copyProperties(heraAppBaseInfo, mqAppBaseInfo);
        return mqAppBaseInfo;
    }
}
