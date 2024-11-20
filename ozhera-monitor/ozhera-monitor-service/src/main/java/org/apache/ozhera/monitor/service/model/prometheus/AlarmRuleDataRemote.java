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

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author gaoxihui
 * @date 2021/9/17 2:25 PM
 */
@Data
public class AlarmRuleDataRemote implements Serializable {


    private Integer id;//int	alert id
    private String cname;//	alert Aliases
    private String alert;//	alert name
    private String  expr;//	expression
    private Map labels;//	map	labels
    private String annotations	;//	Alarm description
    private String group	;//	group name
    private String env	;//	Configure the environment
    Boolean enabled;// bool	Is it enabled
    private String priority	;//	Alarm level
    List<AlertTeamData> alert_team;// Alarm Groups
    private String created_by	;//	creator
    Long created_time;

}
