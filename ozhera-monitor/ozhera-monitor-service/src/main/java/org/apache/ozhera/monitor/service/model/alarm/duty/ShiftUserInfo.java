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

/**
 * @author gaoxihui
 * @date 2023/6/9 2:56 下午
 */
@Data
public class ShiftUserInfo implements Serializable {

    private String user;//Duty person email prefix
    private String display_name;//User alias (Chinese name)
    private Integer start_time;//Start time
    private Long end_time;//End of watch time
    private Long acquire_time;//Time of claim, 10-digit timestamp. 0 indicates that it is not claimed
    private Integer acquire_status;//Claim status, 0= unclaimed, 1= claimed
    private Boolean replace_duty;//The value is true, false, or empty
    private UserInfo should_oncall_user;//The original person on duty during that time

}
