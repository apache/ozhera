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
package org.apache.ozhera.log.manager.user;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/9/7 10:36
 */
public interface MoneUserDetailService {

    Gson GSON = new Gson();

    /**
     * Query user details
     *
     * @param userName
     * @return
     */
    UseDetailInfo queryUserByUserName(String userName);

    /**
     * Query user details
     *
     * @param uId
     * @return
     */
    UseDetailInfo queryUser(String uId);

    /**
     * Query the user's unique ID based on the mobile phone
     *
     * @param phone
     * @return
     */
    String queryUserUIdByPhone(String phone);

    /**
     * Query the user's unique ID based on the employee number
     *
     * @param empId
     * @return
     */
    String queryUserUIdByEmpId(String empId);

    /**
     * Query the user's unique ID based on the user name
     *
     * @param email
     * @return
     */
    String queryUserUIdByUserName(String email);

    JsonArray queryChildDept(String deptId);

    List<String> queryDeptPersonIds(String deptId);
}
