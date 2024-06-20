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
package run.mone.chaos.operator.controller;

import com.xiaomi.mone.tpc.common.vo.ProjectVoV2;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import com.xiaomi.youpin.docean.anno.RequestParam;
import com.xiaomi.youpin.docean.mvc.ContextHolder;
import com.xiaomi.youpin.docean.mvc.MvcResult;
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.bo.PipelineBO;
import run.mone.chaos.operator.service.ProjectService;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Controller
public class ProjectController {

    @Resource
    private ProjectService projectService;

    @RequestMapping(path = "/project/queryProject", method = "post")
    public MvcResult<List<ProjectVoV2>> queryProject(PipelineBO pipelineBO) {
        String account = ((AuthUserVo) ContextHolder.getContext().get().getSession().getAttribute("TPC_USER")).getAccount();
        Integer userType = ((AuthUserVo) ContextHolder.getContext().get().getSession().getAttribute("TPC_USER")).getUserType();
        MvcResult mvcResult = new MvcResult<>();
        mvcResult.setData(projectService.getProjectFromTpc(pipelineBO.getProjectName(), account, userType));
        return mvcResult;
    }

}
