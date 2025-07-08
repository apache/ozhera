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
package org.apache.ozhera.monitor.result;

public class ErrorInfoConstants {

    public static final String SERVER_INTERNAL_ERROR = "服务器内部错误";
    public static final String SCENE_HAS_BEEN_DELETED = "场景已删除";
    public static final String SCENE_OP_DELETE_ERROR_BY_MAPPING="该场景下现在关联指标，需要解除所有场景/指标关联关系后才能删除场景";

    public static final String INDICATOR_OP_DELETE_ERROR_BY_MAPPING = "存在场景关联，需要解除所有场景/指标关联关系后才能删除指标";
    public static final String INDICATOR_HAS_BEEN_DELETED = "指标已删除";

}
