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

import com.google.gson.Gson;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import com.xiaomi.youpin.docean.anno.RequestParam;
import com.xiaomi.youpin.docean.mvc.ContextHolder;
import com.xiaomi.youpin.docean.mvc.MvcResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Pair;
import run.mone.chaos.operator.aspect.anno.ParamValidate;
import run.mone.chaos.operator.bo.CreateChaosTaskBo;
import run.mone.chaos.operator.bo.PipelineBO;
import run.mone.chaos.operator.bo.TaskQryParam;
import run.mone.chaos.operator.bo.chaosBot.CreateBotCronBo;
import run.mone.chaos.operator.common.Config;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dto.page.PageData;
import run.mone.chaos.operator.service.*;
import run.mone.chaos.operator.vo.ChaosTaskDetailVO;
import run.mone.chaos.operator.vo.ChaosTaskInfoVO;
import run.mone.chaos.operator.vo.ChaosTaskReportVO;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author zhangxiaowei6
 * @author caohbaoyu
 * @author zhangping
 * @desc chaosTask的前台界面操作
 * @Date 2023/12/15 11:24
 */
@Controller
@Slf4j
public class ChaosTaskController {
    @Resource
    private ChaosTaskService chaosTaskService;

    @Resource
    private UserService userService;

    @Resource
    private LockService lockService;

    @Resource
    private ProjectService projectService;

    @Resource
    private ChaosBackendService backendService;

    private static Gson gson = new Gson();

    @ParamValidate
    @RequestMapping(path = "/chaosTask/createChaosTask")
    public MvcResult<String> createChaosTask(CreateChaosTaskBo createChaosTaskBo) {
        MvcResult<String> mvcResult = new MvcResult<>();
        AuthUserVo userVo = ((AuthUserVo) ContextHolder.getContext().get().getSession().getAttribute("TPC_USER"));
        if (ObjectUtils.isEmpty(userVo)) {
            mvcResult.setCode(500);
            mvcResult.setMessage("用户未登录");
            return mvcResult;
        }
        Pair<Integer, String> integerStringPair = projectService.checkUserProjectAuth(createChaosTaskBo.getProjectName(), userVo);
        if (integerStringPair.getLeft() != 0) {
            mvcResult.setCode(500);
            mvcResult.setMessage(integerStringPair.getRight());
            return mvcResult;
        }

        if (ModeEnum.ANY.type() == createChaosTaskBo.getMode()) {
            Optional<String> checkPodNum = checkPodNum(createChaosTaskBo);
            if (checkPodNum.isPresent()) {
                mvcResult.setCode(500);
                mvcResult.setMessage(checkPodNum.get());
                return mvcResult;
            }
        }

        createChaosTaskBo.setCreateUser(userVo.getAccount());
        mvcResult.setData(saveTask(createChaosTaskBo));
        return mvcResult;
    }

    @ParamValidate
    @RequestMapping(path = "/chaosTask/updateChaosTask")
    public MvcResult<String> updateChaoTask(CreateChaosTaskBo createChaosTaskBo) {
        MvcResult<String> mvcResult = new MvcResult<>();
        ChaosTask chaosTask = chaosTaskService.getChaosTaskById(createChaosTaskBo.getId());
        AuthUserVo userVo = ((AuthUserVo) ContextHolder.getContext().get().getSession().getAttribute("TPC_USER"));
        if (ObjectUtils.isEmpty(chaosTask)) {
            mvcResult.setCode(500);
            mvcResult.setMessage("实验任务不存在");
            return mvcResult;
        }
        if (!userVo.getAccount().equals(chaosTask.getCreateUser()) && !backendService.isAdmin(userVo.getAccount())) {
            mvcResult.setCode(500);
            mvcResult.setMessage("不能修改非自己创建的实验任务");
            return mvcResult;
        }
        if (ModeEnum.ANY.type() == createChaosTaskBo.getMode()) {
            Optional<String> checkPodNum = checkPodNum(createChaosTaskBo);
            if (checkPodNum.isPresent()) {
                mvcResult.setCode(500);
                mvcResult.setMessage(checkPodNum.get());
                return mvcResult;
            }
        }

        Pair<Integer, String> integerStringPair = projectService.checkUserProjectAuth(createChaosTaskBo.getProjectName(), userVo);
        if (integerStringPair.getLeft() != 0) {
            mvcResult.setCode(500);
            mvcResult.setMessage(integerStringPair.getRight());
            return mvcResult;
        }
        String account = ((AuthUserVo) ContextHolder.getContext().get().getSession().getAttribute("TPC_USER")).getAccount();
        createChaosTaskBo.setCreateUser(account);
        createChaosTaskBo.setExecutedTimes(chaosTask.getExecutedTimes());
        String insertId = saveTask(createChaosTaskBo);
        mvcResult.setData(insertId);
        return mvcResult;
    }

    private String saveTask(CreateChaosTaskBo createChaosTaskBo) {
        TaskEnum taskEnum = TaskEnum.fromType(createChaosTaskBo.getTaskType());
        Class<? extends PipelineBO> boClass = taskEnum.getBoClass();
        PipelineBO pipelineBO = gson.fromJson(createChaosTaskBo.getOperateParam(), boClass);

        Class<? extends TaskBaseService> serviceClass = taskEnum.getServiceClass();
        TaskBaseService taskBaseService = Ioc.ins().getBean(serviceClass);
        return taskBaseService.save(createChaosTaskBo, pipelineBO, StatusEnum.un_action.type());
    }


    @RequestMapping(path = "/chaosTask/getContainers")
    public MvcResult<List<String>> getContainers(CreateChaosTaskBo createChaosTaskBo) {
        MvcResult<List<String>> mvcResult = new MvcResult<>();
        mvcResult.setData(chaosTaskService.getContainers(createChaosTaskBo.getProjectId(), createChaosTaskBo.getPipelineId()));
        return mvcResult;
    }

    @RequestMapping(path = "/chaosTask/execute", method = "get")
    public MvcResult<String> execute(@RequestParam(value = "id") String id) {
        MvcResult<String> mvcResult = new MvcResult<>();
        AuthUserVo userVo = ((AuthUserVo) ContextHolder.getContext().get().getSession().getAttribute("TPC_USER"));
        if (ObjectUtils.isEmpty(userVo)) {
            mvcResult.setCode(500);
            mvcResult.setMessage("用户未登录");
            return mvcResult;
        }
        ChaosTask chaosTaskById = chaosTaskService.getChaosTaskById(id);
        if (chaosTaskById == null || chaosTaskById.getCreateUser().isEmpty()) {
            mvcResult.setCode(500);
            mvcResult.setMessage("任务不存在或者任务异常!");
            return mvcResult;
        }
        if (!userVo.getAccount().equals(chaosTaskById.getCreateUser()) && !backendService.isAdmin(userVo.getAccount())) {
            mvcResult.setCode(500);
            mvcResult.setMessage("不能执行非自己创建的实验任务");
            return mvcResult;
        }
        Pair<Integer, String> integerStringPair = projectService.checkUserProjectAuth(chaosTaskById.getProjectName(), userVo);
        if (integerStringPair.getLeft() != 0) {
            mvcResult.setCode(500);
            mvcResult.setMessage(integerStringPair.getRight());
            return mvcResult;
        }

        // 前置检查
        String checkRes = preCheck(chaosTaskById);
        if (!"ok".equals(checkRes)) {
            mvcResult.setCode(500);
            mvcResult.setMessage(checkRes);
            return mvcResult;
        }

        chaosTaskById.setUpdateUser(userVo.getAccount());
        chaosTaskById.setExecutedTimes(chaosTaskById.getExecutedTimes() + 1);

        chaosTaskService.insertExeLog(chaosTaskById, userVo.getAccount());

        TaskEnum taskEnum = TaskEnum.fromType(chaosTaskById.getTaskType());
        Class<? extends TaskBaseService> serviceClass = taskEnum.getServiceClass();
        TaskBaseService taskBaseService = Ioc.ins().getBean(serviceClass);

        mvcResult.setData(taskBaseService.execute(chaosTaskById));
        return mvcResult;
    }

    @RequestMapping(path = "/chaosTask/recover", method = "get")
    public MvcResult<String> recover(@RequestParam(value = "id") String id) {
        MvcResult<String> mvcResult = new MvcResult<>();
        AuthUserVo userVo = ((AuthUserVo) ContextHolder.getContext().get().getSession().getAttribute("TPC_USER"));
        ChaosTask chaosTaskById = chaosTaskService.getChaosTaskById(id);
        if (ObjectUtils.isEmpty(chaosTaskById)) {
            mvcResult.setCode(500);
            mvcResult.setMessage("任务实验不存在");
            return mvcResult;
        }
        if (chaosTaskById.getStatus() != StatusEnum.actioning.type()) {
            mvcResult.setCode(500);
            String adminStr = backendService.getAdminStr().getData();
            mvcResult.setMessage("异常：实验状态不为actioned,请联系" + adminStr + "!");
            return mvcResult;
        }
        if (!userVo.getAccount().equals(chaosTaskById.getCreateUser()) && !backendService.isAdmin(userVo.getAccount())) {
            mvcResult.setCode(500);
            mvcResult.setMessage("不能恢复非自己创建的实验任务");
            return mvcResult;
        }

        boolean chaosRecoverLock = lockService.chaosRecoverLock(chaosTaskById.getProjectId(), chaosTaskById.getPipelineId());
        if (!chaosRecoverLock) {
            log.info("chaosRecoverLock fail, id:{}", chaosTaskById.getId().toString());
            mvcResult.setCode(500);
            mvcResult.setMessage("实验任务正在恢复中");
            return mvcResult;
        }

        Pair<Integer, String> integerStringPair = projectService.checkUserProjectAuth(chaosTaskById.getProjectName(), userVo);
        if (integerStringPair.getLeft() != 0) {
            mvcResult.setCode(500);
            mvcResult.setMessage(integerStringPair.getRight());
            return mvcResult;
        }

        String account = ((AuthUserVo) ContextHolder.getContext().get().getSession().getAttribute("TPC_USER")).getAccount();
        chaosTaskById.setUpdateUser(account);

        TaskEnum taskEnum = TaskEnum.fromType(chaosTaskById.getTaskType());
        Class<? extends TaskBaseService> serviceClass = taskEnum.getServiceClass();
        TaskBaseService taskBaseService = Ioc.ins().getBean(serviceClass);
        taskBaseService.recover(chaosTaskById);
        // 执行实验锁解除
        lockService.chaosRecoverUnLock(chaosTaskById.getProjectId(), chaosTaskById.getPipelineId());
        lockService.chaosUnLock(chaosTaskById.getProjectId(), chaosTaskById.getPipelineId());

        return mvcResult;
    }

    @RequestMapping(path = "/chaosTask/getTaskList")
    public MvcResult<PageData<ChaosTaskInfoVO>> getTaskList(TaskQryParam qryParam) {
        log.info("ChaosTaskController.getTaskList param:{}", qryParam);
        MvcResult<PageData<ChaosTaskInfoVO>> mvcResult = new MvcResult<>();
        mvcResult.setData(chaosTaskService.getTaskList(qryParam));
        return mvcResult;
    }

    @RequestMapping(path = "/chaosTask/getChaosTaskDetail", method = "get")
    public MvcResult<ChaosTaskDetailVO> getChaosTaskDetail(@RequestParam(value = "id") String id) {
        MvcResult<ChaosTaskDetailVO> mvcResult = new MvcResult<>();
        mvcResult.setData(chaosTaskService.getChaosTaskDetail(id));
        return mvcResult;
    }


    @RequestMapping(path = "/chaosTask/getTaskType", method = "post")
    public MvcResult<Map<String, Integer>> getTaskType() {
        MvcResult<Map<String, Integer>> mvcResult = new MvcResult<>();
        mvcResult.setData(TaskEnum.toMap());
        return mvcResult;
    }

    @RequestMapping(path = "/chaosTask/getTaskReportList")
    public MvcResult<PageData<ChaosTaskReportVO>> getTaskReportList(TaskQryParam qryParam) {
        MvcResult<PageData<ChaosTaskReportVO>> mvcResult = new MvcResult<>();
        mvcResult.setData(chaosTaskService.getTaskReportList(qryParam));
        return mvcResult;
    }

    @RequestMapping(path = "/chaosTask/deleteTask", method = "get")
    public MvcResult<String> deleteTask(@RequestParam(value = "id") String id) {
        MvcResult<String> mvcResult = new MvcResult<>();
        mvcResult.setData(chaosTaskService.deleteTask(id));
        return mvcResult;
    }

    @RequestMapping(path = "/chaosTask/deleteReport", method = "get")
    public MvcResult<String> deleteReport(@RequestParam(value = "id") String id) {
        MvcResult<String> mvcResult = new MvcResult<>();
        mvcResult.setData(chaosTaskService.deleteReport(id));
        return mvcResult;
    }

    @RequestMapping(path = "/chaosTask/getChaosTaskReportDetail", method = "get")
    public MvcResult<ChaosTaskReportVO> getChaosTaskReportDetail(@RequestParam(value = "id") String id) {
        MvcResult<ChaosTaskReportVO> mvcResult = new MvcResult<>();
        mvcResult.setData(chaosTaskService.getChaosTaskReportDetail(id));
        return mvcResult;
    }


    @RequestMapping(path = "/chaosTask/getStatusType", method = "post")
    public MvcResult<Map<String, Integer>> getStatusType() {
        MvcResult<Map<String, Integer>> mvcResult = new MvcResult<>();
        mvcResult.setData(StatusEnum.toMap());
        return mvcResult;
    }

    private String preCheck(ChaosTask task) {
        if (ObjectUtils.isEmpty(task)) {
            return "任务实验不存在";
        }
        // 进行中状态不能再执行实验,恢复的实验也不能再运行
        if (task.getStatus() == StatusEnum.actioning.type()) {
            return "实验状态异常，当前状态为: " + StatusEnum.fromType(task.getStatus()).typeName();
        }
        // 重入锁校验失败情况
        String isReentrantLock = Config.ins().get("enableChaosReentrantLock", "false");
        String redisValue = String.valueOf(task.getId());
        if (isReentrantLock.equals("true") && !lockService.chaosLock(task.getProjectId(), task.getPipelineId(), redisValue)) {
            return "实验进行中，请稍后重试";
        }
        return "ok";
    }

    private Optional<String> checkPodNum(CreateChaosTaskBo createChaosTaskBo) {
        Integer podNums = chaosTaskService.getPodNums(createChaosTaskBo.getPipelineId());
        if (podNums < createChaosTaskBo.getContainerNum()) {
            return Optional.of("实验容器数量不能大于实验pod数量");
        }
        return Optional.empty();

    }


}
