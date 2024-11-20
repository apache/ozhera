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
package org.apache.ozhera.log.manager.service.impl;

import org.apache.ozhera.log.manager.domain.Tpc;
import org.apache.ozhera.log.manager.model.MilogSpaceParam;
import org.apache.ozhera.log.manager.model.pojo.MilogSpaceDO;
import org.apache.ozhera.log.manager.service.SpaceAuthService;
import com.xiaomi.mone.tpc.common.vo.NodeVo;
import com.xiaomi.mone.tpc.common.vo.PageDataVo;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.infra.rpc.Result;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/4/10 15:00
 */
@Service
public class TpcSpaceAuthService implements SpaceAuthService {

    @Resource
    private Tpc tpc;

    @Override
    public Result saveSpacePerm(MilogSpaceDO spaceDO, String account) {
        Result tpcResult = tpc.saveSpacePerm(spaceDO, account);
        return tpcResult;
    }

    @Override
    public Result<PageDataVo<NodeVo>> getUserPermSpace(String spaceName, Integer page, Integer pageSize) {
        Result<PageDataVo<NodeVo>> tpcRes = tpc.getUserPermSpace(spaceName, page, pageSize);
        return tpcRes;
    }

    @Override
    public Result deleteSpaceTpc(Long spaceId, String account, Integer userType) {
        Result tpcResult = tpc.deleteSpaceTpc(spaceId, account, userType);
        return tpcResult;
    }

    @Override
    public Result updateSpaceTpc(MilogSpaceParam param, String account) {
        Result tpcResult = this.tpc.updateSpaceTpc(param, account);
        return tpcResult;
    }

    @Override
    public void addSpaceMember(Long spaceId, String userAccount, Integer userType, Integer memberCode) {
        tpc.addSpaceMember(spaceId, userAccount, userType, memberCode);
    }

    @Override
    public Result<List<NodeVo>> queryUserListNode(String spaceName, String userName) {
        return Result.success(tpc.queryUserListNode(spaceName, userName));
    }

}
