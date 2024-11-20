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
 * @date 2023/6/8 2:16 下午
 */
@Data
public class DutyInfo implements Serializable {

    private String manager;
    private Integer model_type;//Identification Duty mode: 0= multiple persons on duty, 1= active/standby

    /**
     * When there is a duty table, it is used to mark whether the sending channel of the duty table is sent only to the group, 0= no, 1= yes.
     * When off send to group only (=0), send fly book notification to group and duty person
     * When only sent to the group (=1), P0 does not call the person on duty, and the group fails to send the person on duty again
     */
    private Integer chat_only;
    private List<DutyGroup> child_groups;

}
