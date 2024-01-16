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
package com.xiaomi.youpin.prometheus.agent.aop;

import com.xiaomi.youpin.prometheus.agent.enums.ErrorCode;
import com.xiaomi.youpin.prometheus.agent.param.BaseParam;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;

@Slf4j
@Aspect
@Configuration
public class ArgCheckAspect {

    @Pointcut("@annotation(com.xiaomi.youpin.prometheus.agent.aop.ArgCheck)")
    public void argCheck() {
    }

    @Around("argCheck()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length <= 0) {
            return joinPoint.proceed();
        }
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        ArgCheck argCheck = method.getAnnotation(ArgCheck.class);
        //HTTP request header
        RequestMapping reqMapping = method.getAnnotation(RequestMapping.class);
        Class<?> resultCls = method.getReturnType();
        for (Object arg : args) {
            if (!(arg instanceof com.xiaomi.youpin.prometheus.agent.param.ArgCheck)) {
                continue;
            }
            if (arg instanceof BaseParam) {
                BaseParam baseParam = (BaseParam) arg;
                log.info("接口{}请求参数{}", method.getName(), arg);
                if (!((com.xiaomi.youpin.prometheus.agent.param.ArgCheck) arg).argCheck()) {
                    log.warn("用户请求参数校验失败; arg={}", arg);
                    return getResult(resultCls, ErrorCode.invalidParamError);
                }
            }
            try {
                Object result = joinPoint.proceed();
                log.info("接口{}请求响应{}", method.getName(), result);
                return result;
            } catch (Throwable e) {
                log.error("接口{}请求异常", method.getName(), e);
                return getResult(resultCls, ErrorCode.unknownError);
            }
        }

        try {
            Object result = joinPoint.proceed();
            log.info("接口{}请求响应{}", method.getName(), result);
            return result;
        } catch (Throwable e) {
            log.error("接口{}请求异常", method.getName(), e);
            return getResult(resultCls, ErrorCode.unknownError);
        }
    }

    private Object getResult(Class<?> resultCls, ErrorCode responseCode) {
        return responseCode;
    }
}
