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

import org.apache.ozhera.app.api.model.HeraAppRoleModel;
import lombok.Data;
import lombok.ToString;
import org.springframework.beans.BeanUtils;

/**
 * @author gaoxihui
 * @date 2022/11/23 8:08 PM
 */
@Data
@ToString
public class HeraAppRoleQuery {

    private Integer id;

    private String appId;

    private Integer appPlatform;

    private String user;

    private Integer role;

    private Integer page;

    private Integer pageSize;

    public HeraAppRoleModel getModel(){
        HeraAppRoleModel model = new HeraAppRoleModel();
        BeanUtils.copyProperties(this,model);
        return model;
    }
}
