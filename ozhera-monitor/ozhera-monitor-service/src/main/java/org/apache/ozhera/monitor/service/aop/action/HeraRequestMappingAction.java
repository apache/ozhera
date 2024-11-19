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

/**
 * 通用参数类型
 * @author: zgf1
 * @date: 2022/1/13 15:59
 */
public interface HeraRequestMappingAction {
    /**
     * 数据类型
     */
    int DATA_TYPE_STRATEGY = 1;
    int DATA_TYPE_RULE = 2;
    int DATA_ALERT_GROUP = 3;

    /**
     * 日志类型
     */
    int LOG_TYPE_PARENT = 0;

    /**
     *
     * @param args 请求参数
     * @param heraReqInfo hera收集参数
     */
    void beforeAction(Object[] args, HeraReqInfo heraReqInfo);

    /**
     *
     * @param args 请求参数
     * @param heraReqInfo hera收集参数
     * @param result 执行结果
     */
    void afterAction(Object[] args, HeraReqInfo heraReqInfo, Object result);
}
