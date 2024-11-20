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

/**
 * @author gaoxihui
 * @date 2021/9/16 8:52 PM
 */
@Data
public class AlertTeamData implements Serializable {
    private Integer id;//oncall group id
    String name;//oncall group name
    String note;//oncall group note
    String[] duty_users;//current oncall duty users
    String manager;//duty manager
    String[] members;
    String chat_id;//feishu group ID
    String created_by;
    Long created_time;
    Integer goc_oncall_id;//migoc oncall id

}
