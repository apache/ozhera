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

package org.apache.ozhera.log.api.model.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreInfo implements Serializable {

    private Long storeId;

    private Long spaceId;

    private String storeName;

    private Integer machineRoom;

    private String logType;

    private Integer mqResourceId;

    private String columnTypeList = "date,keyword,keyword,text,text,keyword,keyword,text,keyword,keyword,keyword,keyword,keyword,keyword,keyword,long";

    private String keyList = "timestamp:1,level:1,traceId:1,threadName:1,className:1,line:1,methodName:1,message:1,podName:1,logstore:3,logsource:3,mqtopic:3,mqtag:3,logip:3,tail:3,linenumber:3";

    private String keysName = "";

    private boolean selectCustomIndex;

    private Integer shardCnt = 1;

    private Integer storePeriod = 180;

    private Integer esResourceId;

    private UserInfo userInfo;

    public String isValidParam(boolean isCreate) {
        if (spaceId == null || spaceId < 0) {
            return "The space id cannot be empty!";
        }
        if (storeName == null || storeName.isEmpty()) {
            return "The store name cannot be empty!";
        }
        if (userInfo == null || userInfo.getUser() == null || userInfo.getUser().isEmpty()) {
            return "The user information is empty. Please provide the correct user information!";
        }
        if (!isCreate) {
            if (storeId == null || storeId < 0) {
                return "The store id cannot be empty!";
            }
            if (userInfo.getUserType() == null) {
                return "The user type cannot be empty!";
            }
        }
        return null;
    }

}
