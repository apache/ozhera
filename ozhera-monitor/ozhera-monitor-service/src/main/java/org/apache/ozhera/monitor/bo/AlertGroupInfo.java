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

import org.apache.ozhera.monitor.service.model.alarm.duty.DutyInfo;
import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 *
 * @author zhanggaofeng1
 */
@Data
@ToString
public class AlertGroupInfo {

    private boolean delete;
    private boolean edit;
    private long id;
    private String name;
    private String note;
    private String chatId;
    private List<UserInfo> members;
    private int treeId;
    private String createdBy;
    private long createdTime;
    private String type;
    private long relId;
    private DutyInfo dutyInfo;

}
