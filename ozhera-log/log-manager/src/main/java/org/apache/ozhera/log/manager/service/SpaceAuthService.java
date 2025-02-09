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
package org.apache.ozhera.log.manager.service;

import org.apache.ozhera.log.manager.model.MilogSpaceParam;
import org.apache.ozhera.log.manager.model.pojo.MilogSpaceDO;
import com.xiaomi.mone.tpc.common.vo.NodeVo;
import com.xiaomi.mone.tpc.common.vo.PageDataVo;
import com.xiaomi.youpin.infra.rpc.Result;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/4/10 15:00
 */
public interface SpaceAuthService {

    /**
     * save permission
     *
     * @param spaceDO
     * @param account
     * @return
     */
    Result saveSpacePerm(MilogSpaceDO spaceDO, String account);

    /**
     * get current user have auth space
     *
     * @param spaceName
     * @param page
     * @param pageSize
     * @return
     */
    Result<PageDataVo<NodeVo>> getUserPermSpace(String spaceName, Integer page, Integer pageSize);

    /**
     * delete space
     *
     * @param spaceId
     * @param account
     * @param userType
     * @return
     */
    Result deleteSpaceTpc(Long spaceId, String account, Integer userType);

    /**
     * update space
     *
     * @param param
     * @param account
     * @return
     */
    Result updateSpaceTpc(MilogSpaceParam param, String account);

    /**
     * add member
     *
     * @param spaceId
     * @param userAccount
     * @param userType
     * @param memberCode
     */
    void addSpaceMember(Long spaceId, String userAccount, Integer userType, Integer memberCode);

    /**
     * query access to user node list
     * @param spaceName
     * @param userName
     * @return
     */
    Result<List<NodeVo>> queryUserListNode(String spaceName, String userName);

}
