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
package org.apache.ozhera.log.manager.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/4/28 11:16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickQueryVO implements Serializable {
    private Long spaceId;
    private String spaceName;
    private Long storeId;
    private String storeName;
    private Long tailId;
    private String tailName;
    private Long envId;
    private String envName;
    private Integer isFavourite;
    private String deploySpace;
    private String logPath;
    private String originSystem;

    private Boolean collectionReady;
}
