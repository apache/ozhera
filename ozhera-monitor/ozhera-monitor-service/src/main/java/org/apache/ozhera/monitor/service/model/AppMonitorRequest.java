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

package org.apache.ozhera.monitor.service.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2021/8/13 1:11 PM
 */
@Data
public class AppMonitorRequest implements Serializable {

    String appName;
    Integer page;
    Integer pageSize;
    Integer viewType;//0 我的应用；1 我关注的应用；
    Integer area;//0
    Integer distinct;//1 true 0 false只有在不指定viewType的情况下生效，
    Integer platFormType;//平台类型
    List<ProjectInfo> projectList;
    private long duration = 1800L;//统计最近30分钟告警
    private boolean needPage;
    private Long startTimeCurrent;//当前页签数字统计开始时间
    private Long endTimeCurrent;//当前页签数字统计结束时间
    private Long startTime;//页签数字统计开始时间
    private Long endTime;//页签数字统计结束时间
    private String metricType;//页签数字统计指标类型
    private String methodName;//页签数字统计方法名

    public void qryInit() {
        if (page == null || page <= 0) {
            page = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
        if (pageSize > 99) {
            pageSize = 99;
        }
    }


}
