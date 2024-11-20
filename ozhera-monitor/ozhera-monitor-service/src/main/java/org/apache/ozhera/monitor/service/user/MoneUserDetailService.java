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

package org.apache.ozhera.monitor.service.user;

import com.google.gson.Gson;

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
     * 查询用户详情
     *
     * @param uId
     * @return
     */
    UseDetailInfo queryUser(String uId);

    /**
     * 根据手机查询用户唯一Id
     *
     * @param phone
     * @return
     */
    String queryUserUIdByPhone(String phone);


    /**
     * 根据员工号查询用户唯一Id
     *
     * @param empId
     * @return
     */
    String queryUserUIdByEmpId(String empId);
    String queryUserUIdByUsername(String empId);


    List<String> getWhiteList();

    List<String> getBlackList();

    List<String> getDeptBlackList();

    List<String> getAdminUserList();

}
