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
package run.mone.chaos.operator.service;

import com.google.gson.Gson;
import com.xiaomi.mone.tpc.api.service.NodeFacade;
import com.xiaomi.mone.tpc.api.service.NodeUserFacade;
import com.xiaomi.mone.tpc.api.service.UserOrgFacade;
import com.xiaomi.mone.tpc.common.enums.NodeTypeEnum;
import com.xiaomi.mone.tpc.common.enums.OutIdTypeEnum;
import com.xiaomi.mone.tpc.common.param.*;
import com.xiaomi.mone.tpc.common.vo.NodeVo;
import com.xiaomi.mone.tpc.common.vo.OrgInfoVo;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.common.StringUtils;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Reference;
import com.xiaomi.youpin.infra.rpc.Result;
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.config.MoneUser;

@Slf4j
@Service
public class TpcService {

    @Reference(interfaceClass = NodeFacade.class, group = "$tpc_dubbo_group", check = false, version = "1.0", timeout = 10000)
    private NodeFacade tpcService;

    @Reference(interfaceClass = NodeUserFacade.class, group = "$tpc_dubbo_group", check = false, version = "1.0", timeout = 10000)
    private NodeUserFacade tpcUserService;

    @Reference(interfaceClass = UserOrgFacade.class, group = "$tpc_dubbo_group", check = false, version = "1.0", timeout = 10000)
    private UserOrgFacade userOrgFacade;

    @Value("${tpc_node_code}")
    private String tpcNodeCode;

    Gson gson = new Gson();

    private Long tpcPId;

    public boolean isAdmin(String account, Integer userType) throws Exception {
        handleRemoteTpcId(tpcNodeCode, account, userType);
        NodeQryParam param = new NodeQryParam();
        param.setAccount(account);
        param.setUserType(userType);
        param.setId(tpcPId);
        param.setType(NodeTypeEnum.PRO_TYPE.getCode());
        Result<NodeVo> nodeVoResult = tpcService.get(param);
        NodeVo node = nodeVoResult.getData();
        log.debug("get user isAdmin,param:{},result:{}", gson.toJson(param), gson.toJson(node));
        return node.isCurrentMgr() || node.isTopMgr() || node.isParentMgr();
    }



    private NodeQryParam getNodeParam(String account, Integer userType) {
        NodeQryParam nodeParam = new NodeQryParam();
        nodeParam.setAccount(account);
        nodeParam.setUserType(userType);
        nodeParam.setType(NodeTypeEnum.PRO_TYPE.getCode());
        nodeParam.setNodeCode(tpcNodeCode);
        return nodeParam;
    }

    public boolean hasPerm(MoneUser user, Long spaceId) {
        NodeQryParam param = new NodeQryParam();
        param.setAccount(user.getUser());
        param.setUserType(user.getUserType());
        param.setOutId(spaceId);
        param.setOutIdType(OutIdTypeEnum.SPACE.getCode());
        Result<NodeVo> nodeVoResult = tpcService.getByOutId(param);
        NodeVo node = nodeVoResult.getData();
        return node.isTopMgr() || node.isParentMgr() || node.isCurrentMgr();
    }


    public OrgInfoVo getOrg(String account, Integer userType) {
        NullParam param = new NullParam();
        param.setAccount(account);
        param.setUserType(userType);
        Result<OrgInfoVo> res = userOrgFacade.getOrgByAccount(param);
        if (res == null || res.getCode() != 0) {
            log.warn("Failed to find user department,account:[{}], userType:[{}], res:[{}]", account, userType, res);
            return new OrgInfoVo();
        }
        return res.getData();
    }

    public void handleRemoteTpcId(String tpcNodeCode, String account, Integer userType) throws Exception {
        if (StringUtils.isEmpty(tpcNodeCode)) {
            throw new Exception("tpc_node_code is empty,please check config file");
        }
        if (null == tpcPId) {
            Result<NodeVo> nodeVoResult = tpcService.getByNodeCode(getNodeParam(account, userType));
            tpcPId = nodeVoResult.getData().getId();
        }
        if (null == tpcPId) {
            throw new Exception("query tpc id by tpc server error,tpc code:" + tpcNodeCode);
        }
    }
}
