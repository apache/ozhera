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
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 *
 * @author zhanggaofeng1
 */
@Data
public class AlertGroupParam {

    private long id;
    private List<Integer> relIds;
    private Integer page;
    private Integer pageSize;
    private String name;
    private String note;
    private String chatId;
    private List<Long> memberIds;
    private String type;
    private DutyInfo dutyInfo;
    private Long start;
    private Long end;
    public void pageQryInit() {
        if (page == null || page <= 0) {
            page = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 50;
        }
        if (pageSize >= 100) {
            pageSize = 99;
        }
    }

    public boolean createArgCheck() {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        if (StringUtils.isBlank(note)) {
            return false;
        }
        if (memberIds == null || memberIds.isEmpty()) {
            return false;
        }
        return true;
    }

}
