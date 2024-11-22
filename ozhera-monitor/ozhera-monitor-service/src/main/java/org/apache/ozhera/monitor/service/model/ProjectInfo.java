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

package org.apache.ozhera.monitor.service.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author gaoxihui
 * @date 2021/7/9 9:35 AM
 */
@Data
public class ProjectInfo implements Serializable {
    private Long id;

    private String name;

    private String gitName;

    private String mioneEnv;

    private String desc;

    private long ctime;

    private long utime;

    private String domain;

    private Long iamTreeId;
}
