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
package org.apache.ozhera.monitor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指标权限检查注解
 * 用于标记需要进行指标权限验证的方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireIndicatorPermission {

    /**
     * 指标ID参数名，支持SpEL表达式
     * 例如：
     * - "indicatorId" : 直接参数名
     * - "req.id" : 对象属性
     * - "#req.id" : SpEL表达式
     */
    String indicatorIdParam() default "req.id";

    /**
     * 用户账号参数名，支持SpEL表达式
     * 例如：
     * - "userAccount" : 直接参数名
     * - "req.operatorAccount" : 对象属性
     * - "#req.operatorAccount" : SpEL表达式
     */
    String userAccountParam() default "req.operatorAccount";

    /**
     * 是否允许指标创建者访问（即使没有显式权限）
     */
    boolean allowCreator() default true;

    /**
     * 操作类型描述，用于日志记录
     */
    String operation() default "";

    /**
     * 当权限检查失败时是否抛出异常
     * true: 抛出异常
     * false: 返回null或false（根据方法返回类型）
     */
    boolean throwException() default false;
}