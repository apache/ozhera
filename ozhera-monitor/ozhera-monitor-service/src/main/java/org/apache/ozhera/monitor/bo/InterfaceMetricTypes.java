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
 * @date 2022/5/5 5:31 下午
 */
public enum InterfaceMetricTypes {
    error_times("error_times"),
    availability("availability"),
    qps("qps"),
    slow_times("slow_times"),
    time_cost("time_cost"),
    basic("basic"),
    jvm_runtime("jvm_runtime"),
    application("application"),
    mione_container("mione_container"),
    container("container"),
    instance("instance"),
    matrix_deploy_unit("matrix_deploy_unit"),
    proxy_mife("proxy_mife"),
    thread_pool("thread_pool"),
    ;

    private String name;

    InterfaceMetricTypes(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
