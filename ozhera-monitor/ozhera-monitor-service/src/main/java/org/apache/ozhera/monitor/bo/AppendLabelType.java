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
package org.apache.ozhera.monitor.bo;

/**
 * @author gaoxihui
 * @date 2021/11/12 2:38 下午
 */
public enum AppendLabelType {
    http_include_uri,
    http_except_uri,
    http_include_errorCode,
    http_except_errorCode,
    http_client_inclue_domain,
    http_client_excpet_domain,
    dubbo_include_method,
    dubbo_except_method,
    dubbo_include_service,
    dubbo_except_service;
}
