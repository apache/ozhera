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

package org.apache.ozhera.monitor.service.prometheus;

/**
 * @author gaoxihui
 * @date 2021/7/22 4:42 下午
 */
public class PromQL {

    // dubbo
    public static final String DUBBO_BIS_ERROR_COUNT = "dubboBisErrorCount";
    public static final String DUBBO_ERROR_RPC_CALL_TIME = "dubboErrorRpcCallTime";
    public static final String DUBBO_CONSUMER_TIME_COST = "dubboConsumerTimeCost";

    // 时间记录aop
    public static final String AOP_ERROR_METHOD_COUNT = "aopErrorMethodCount";
    public static final String AOP_METHOD_TIME_COUNT = "aopMethodTimeCount";

    // sql
    public static final String SQL_ERROR_COUNT = "sqlErrorCount";
    public static final String SQL_TIME_OUT_COUNT = "sqlTimeOutCount";

    // redis
    public static final String REDIS_FAILED_COUNT = "RedisFailedCount"; //失败方法调用
    public static final String REDIS_SLOW_QUERY = "RedisSlowQuery";

}
