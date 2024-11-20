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

package org.apache.ozhera.monitor.service.model.prometheus;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.ozhera.monitor.dao.model.AppAlarmRule;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author gaoxihui
 * @date 2021/9/15 5:40 下午
 */
@Data
public class AppWithAlarmRules implements Serializable {

    String appName;
    String creater;
    Integer ruleStatus;//0 生效、1暂停
    Integer iamId;
    Integer projectId;
    String remark;
    Map<String, String> metricMap;
    Map<String, String> checkDataMap;
    Map<String, String> sendIntervalMap;


    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    Date lastUpdateTime;
    List<AppAlarmRule> alarmRules;
}
