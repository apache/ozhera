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
package org.apache.ozhera.monitor.aop;

import org.apache.ozhera.monitor.bo.HeraReqInfo;
import org.apache.ozhera.monitor.service.aop.action.HeraRequestMappingAction;
import org.apache.ozhera.monitor.service.aop.context.HeraRequestMappingContext;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @project: mimonitor
 * @author: zgf1
 * @date: 2022/1/13 14:39
 */
@Slf4j
@Aspect
@Configuration
public class HeraRequestMappingAspect {

    @Pointcut("@annotation(org.apache.ozhera.monitor.aop.HeraRequestMapping)")
    public void operationLog(){}

    @Autowired
    private ApplicationContext applicationContext;

    @Resource(name = "heraRequestMappingExecutor")
    private ThreadPoolExecutor heraRequestMappingExecutor;

    @Around("operationLog()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
            Method method = methodSignature.getMethod();
            Object[] args = joinPoint.getArgs();
            if (args == null) {
                args = new Object[0];
            }
            String reqUrl = null;
            String user = null;
            for (Object arg : args) {
                if (arg instanceof HttpServletRequest) {
                    reqUrl = ((HttpServletRequest)arg).getServletPath();
                 //   UserInfoVO userInfo = AegisFacade.getUserInfo((HttpServletRequest)arg);
                    AuthUserVo userInfo = UserUtil.getUser();
                    if (userInfo != null) {
                        user = userInfo.genFullAccount();
                    }
                    break;
                }
            }
            HeraRequestMapping anno = method.getAnnotation(HeraRequestMapping.class);
            HeraReqInfo heraReqInfo = HeraReqInfo.builder().reqUrl(reqUrl).user(user)
                    .moduleName(anno.interfaceName().getModuleName().getCode())
                    .interfaceName(anno.interfaceName().getCode()).build();
            Object beanObj = applicationContext.getBean(anno.actionClass());
            if (beanObj instanceof HeraRequestMappingAction) {
                HeraRequestMappingAction beanAction = (HeraRequestMappingAction)beanObj;
                beanAction.beforeAction(args, heraReqInfo);
                Object result = null;
                try {
                    result = joinPoint.proceed();
                    return result;
                } finally {
                    if (heraReqInfo.getOperLog() != null && heraReqInfo.getOperLog().getId() != null) {
                        final Object aResult = result;
                        final Object[] aArgs = args;
                        final Map<String, Object> map = HeraRequestMappingContext.getAll();
                        heraRequestMappingExecutor.execute(new Runnable() {
                            @Override
                            public void run() {
                                HeraRequestMappingContext.putAll(map);
                                try {
                                    beanAction.afterAction(aArgs, heraReqInfo, aResult);
                                } finally {
                                    HeraRequestMappingContext.clearAll();
                                }
                            }
                        });
                    }
                }
            }
            return joinPoint.proceed();
        } finally {
            HeraRequestMappingContext.clearAll();
        }
    }
}
