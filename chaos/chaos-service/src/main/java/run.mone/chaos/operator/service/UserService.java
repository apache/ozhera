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

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.mone.tpc.api.service.NodeUserFacade;
import com.xiaomi.mone.tpc.api.service.UserFacade;
import com.xiaomi.mone.tpc.api.service.UserOrgFacade;
import com.xiaomi.mone.tpc.common.enums.NodeUserRelTypeEnum;
import com.xiaomi.mone.tpc.common.param.NodeUserQryParam;
import com.xiaomi.mone.tpc.common.param.NullParam;
import com.xiaomi.mone.tpc.common.vo.NodeUserRelVo;
import com.xiaomi.mone.tpc.common.vo.OrgInfoVo;
import com.xiaomi.mone.tpc.common.vo.PageDataVo;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.mvc.ContextHolder;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Reference;
import com.xiaomi.youpin.infra.rpc.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import run.mone.chaos.operator.vo.UserInfoVo;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Reference(group = "${ref.tpc.service.group}", interfaceClass = NodeUserFacade.class, version = "1.0")
    private NodeUserFacade nodeUserFacade;

    @Reference(group = "${ref.tpc.service.group}", interfaceClass = UserOrgFacade.class, version = "1.0")
    private UserOrgFacade userOrgFacade;

    @Reference(group = "${ref.tpc.service.group}", interfaceClass = UserFacade.class, version = "1.0")
    private UserFacade userFacade;

    @Resource
    private ChaosBackendService backendService;

    public UserInfoVo getUserInfo() {
        AuthUserVo userVo = ((AuthUserVo) ContextHolder.getContext().get().getSession().getAttribute("TPC_USER"));
        if (userVo == null) {
            return null;
        }
        UserInfoVo userInfo = new UserInfoVo();
        userInfo.setDisplayName(userVo.getName());
        userInfo.setEmail(userVo.getEmail());
        userInfo.setName(userVo.getName());
        userInfo.setUsername(userVo.getAccount());
        userInfo.setFullAccount(userVo.genFullAccount());
        userInfo.setAvatar(userVo.getAvatarUrl());
        Optional<OrgInfoVo> userOrgInfo = getUserOrgInfo(userVo.getAccount());
        userInfo.setTenant(!userOrgInfo.isPresent() ? "" : userOrgInfo.get().getIdPath());
        userInfo.setUserType(userVo.getUserType());
        userInfo.setAdmin(backendService.isAdmin(userVo.getAccount()));
        return userInfo;
    }

    /**
     * 根据用户名获取用户组织信息，若成功则返回包含OrgInfoVo的Optional，失败则记录警告并返回空的Optional。
     */
    public Optional<OrgInfoVo> getUserOrgInfo(String userName) {
        NullParam param = new NullParam();
        param.setAccount(userName);
        param.setUserType(0);
        try {
            return Optional.ofNullable(userOrgFacade.getOrgByAccount(param).getData());
        } catch (Exception e) {
            log.warn("[LoginService.getAccountFromSession], failed to getOrgByAccount, msg: {} ", e.getMessage());
        }
        return Optional.empty();
    }

}
