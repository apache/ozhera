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

import com.xiaomi.mone.tpc.api.service.ProjectFacade;
import com.xiaomi.mone.tpc.common.param.ProjectQryParam;
import com.xiaomi.mone.tpc.common.vo.ProjectVoV2;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Reference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class ProjectService {

    @Reference(interfaceClass = ProjectFacade.class, group = "$ref.tpc.service.group", check = true, timeout = 15000, version = "1.0")
    private ProjectFacade projectFacade;

    @Resource
    private ChaosBackendService backendService;

    @Value("$is.local")
    private String isLocal;

    public Pair<Integer, String> checkUserProjectAuth(String projectName, AuthUserVo userVo) {
        if ("true".equals(isLocal)) {
            return Pair.of(0, "ok");
        }
        List<ProjectVoV2> projectFromTpc = getProjectFromTpc(projectName, userVo.getAccount(), userVo.getUserType());
        // 如果用户没有项目权限，并且不是admin，则返回错误
        if (CollectionUtils.isEmpty(projectFromTpc) && !backendService.isAdmin(userVo.getAccount())) {
            return Pair.of(-1, "用户没有该项目权限");
        }
        return Pair.of(0, "ok");
    }

    public List<ProjectVoV2> getProjectFromTpc(String projectName, String account, Integer userType) {
        ProjectQryParam projectQryParam = new ProjectQryParam();
        projectQryParam.setAccount(account);
        projectQryParam.setName(projectName);
        projectQryParam.setUserType(userType);
        projectQryParam.setNeedTopSort(true);
        return projectFacade.search(projectQryParam).getData();
    }

    public List<ProjectVoV2> getProjectFromTpcWithOutProjectName(String account, Integer userType) {
        ProjectQryParam projectQryParam = new ProjectQryParam();
        projectQryParam.setAccount(account);
        projectQryParam.setUserType(userType);
        projectQryParam.setNeedTopSort(true);
        return projectFacade.search(projectQryParam).getData();
    }


}
