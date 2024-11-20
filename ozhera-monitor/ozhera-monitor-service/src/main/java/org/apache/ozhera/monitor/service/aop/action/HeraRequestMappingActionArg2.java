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

package org.apache.ozhera.monitor.service.aop.action;

import org.apache.ozhera.monitor.bo.HeraReqInfo;
import org.apache.ozhera.monitor.dao.HeraOperLogDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 两个参数类型
 * @author: zgf1
 * @date: 2022/1/13 15:59
 */
@Slf4j
public abstract class HeraRequestMappingActionArg2<REQUEST1, REQUEST2, RESULT> implements HeraRequestMappingAction {

    @Autowired
    protected HeraOperLogDao HeraOperLogDao;

    /**
     *
     * @param args 请求参数
     * @param heraReqInfo hera收集参数
     */
    @Override
    public void beforeAction(Object[] args, HeraReqInfo heraReqInfo) {
        if (args != null && args.length != 2) {
            return;
        }
        try {
            beforeAction((REQUEST1)args[0], (REQUEST2)args[1], heraReqInfo);
        } catch (Throwable e) {
            log.info("操作日志执行前异常;heraReqInfo={}", heraReqInfo, e);
        }
    }

    public abstract void beforeAction(REQUEST1 arg1, REQUEST2 arg2, HeraReqInfo heraReqInfo);

    /**
     *
     * @param args 请求参数
     * @param heraReqInfo hera收集参数
     * @param result 执行结果
     */
    @Override
    public void afterAction(Object[] args, HeraReqInfo heraReqInfo, Object result) {
        if (args != null && args.length != 2) {
            return;
        }
        try {
            afterAction((REQUEST1)args[0], (REQUEST2)args[1], heraReqInfo, (RESULT)result);
        } catch (Throwable e) {
            log.info("操作日志执行后异常;heraReqInfo={}", heraReqInfo, e);
        }
    }

    public abstract void afterAction(REQUEST1 arg1, REQUEST2 arg2, HeraReqInfo heraReqInfo, RESULT result);
}
