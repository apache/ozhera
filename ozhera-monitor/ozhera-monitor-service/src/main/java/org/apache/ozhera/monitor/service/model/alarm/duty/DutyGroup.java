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

package org.apache.ozhera.monitor.service.model.alarm.duty;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2023/6/8 2:06 下午
 */
@Data
public class DutyGroup implements Serializable {

    private Integer id;//id
    private String name;//Sub-duty table (sub-alarm group) name. The name cannot be the same
    private Integer rotation_type=0;//Duty period type: 0 is day, 1 is week, 2 is custom [Default value is 0]
    private Integer shift_length=0;//Custom period, takes effect when RotationType=2 [Default value is 0]
    private String shift_length_unit="";//User-defined period unit: days or weeks
    private Long duty_start_time;//Start of shift, 10-digit timestamp
    private Long handoff_time;//Shift handover time, time point, unit is seconds. For example, 43200=12:00
    private Integer preset_vacation=0;//Mark whether a scheduled holiday is on duty, 0= no, 1= yes. (Default is 0) When Yes (=1), the oncall_vacations field cannot be empty
    private List<UserInfo> oncall_vacations;//Holiday duty information. Holiday duty personnel must be a subset of daily duty personnel (oncall_users)
    private List<UserInfo> oncall_users;//Duty list information

    private ShiftUserInfo duty_user;//Current duty officer
    private ShiftUserInfo next_duty_user;//Next shift officer
    private List<ShiftUserInfo> duty_order_users;//Actual duty schedule

}
