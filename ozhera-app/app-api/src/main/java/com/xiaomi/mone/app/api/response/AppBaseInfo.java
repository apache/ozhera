/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.mone.app.api.response;

import lombok.Data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/2 11:45
 */
@Data
public class AppBaseInfo implements Serializable {
    private Integer id;
    private String bindId;
    private String appName;
    private String appCname;
    private Integer platformType;
    private String platformName;
    private Integer appType;
    private String appTypeName;
    private List<Integer> treeIds;
    private LinkedHashMap<String, List<String>> nodeIPs;
}
