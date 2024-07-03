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
import com.xiaomi.youpin.docean.anno.Service;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import run.mone.chaos.operator.bo.TaskQryParam;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.ChaosTaskLog;
import run.mone.chaos.operator.dao.domain.ChaosTaskReport;
import run.mone.chaos.operator.dao.domain.TaskExeLog;
import run.mone.chaos.operator.dao.impl.ChaosTaskDao;
import run.mone.chaos.operator.dao.impl.ChaosTaskLogDao;
import run.mone.chaos.operator.dao.impl.ChaosTaskReportDao;
import run.mone.chaos.operator.dao.impl.TaskExeLogDao;
import run.mone.chaos.operator.dto.page.PageData;
import run.mone.chaos.operator.vo.*;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author zhangxiaowei6
 * @author caobaoyu
 * @Date 2023/12/15 11:26
 */
@Slf4j
@Service
public class ChaosTaskService {

    @Resource
    private ChaosTaskDao taskDao;

    @Resource
    private ChaosTaskReportDao taskReportDao;

    @Resource
    private ChaosTaskLogDao taskLogDao;

    @Resource
    private TaskExeLogDao exeLogDao;


    private static Gson gson = new Gson();

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    public PageData<ChaosTask> getListByMap(Map<String, Object> map) {
        try {
            PageData<ChaosTask> pageData = new PageData<>();
            return taskDao.getListByMap(pageData, map);
        } catch (Exception e) {
            log.error("getListByMap error:", e);
        }
        return null;
    }

    public ChaosTask getChaosTaskById(String id) {
        return taskDao.getById(id, ChaosTask.class);
    }

    public List<ChaosTaskLog> getChaosTaskLogByTaskIdAndExecutedTimesAndStatus(int executedTimes, String taskId, int status) {
        List<ChaosTaskLog> logList = Lists.newArrayList();
        PageData<ChaosTaskLog> pageData = new PageData<>();
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", taskId);
        map.put("executedTimes", executedTimes);
        map.put("status", status);
        PageData<ChaosTaskLog> initLogData = taskLogDao.getListByMap(pageData, map);
        logList.addAll(initLogData.getList());
        return logList;
    }

    public List<ChaosTaskReport> getChaosTaskReportByTaskIdAndExecutedTimes(int executedTimes, String taskId) {
        List<ChaosTaskReport> reportList = Lists.newArrayList();
        PageData<ChaosTaskReport> pageData = new PageData<>();
        Map<String, Object> map = new HashMap<>();
        map.put("experimentId", taskId);
        map.put("executedTimes", executedTimes);
        PageData<ChaosTaskReport> listByMap = taskReportDao.getListByMap(pageData, map);
        reportList.addAll(listByMap.getList());
        return reportList;
    }


    private Map<String, Object> buildQueryParam(TaskQryParam qryParam) {
        Map<String, Object> map = new HashMap<>();
        if (qryParam.getTaskType() != null) {
            map.put("taskType", qryParam.getTaskType());
        }
        if (qryParam.getStatus() != null) {
            map.put("status", qryParam.getStatus());
        }
        if (StringUtils.isNotBlank(qryParam.getExperimentName())) {
            map.put("experimentName", qryParam.getExperimentName());
        }
        if (StringUtils.isNotBlank(qryParam.getProjectName())) {
            map.put("projectName", qryParam.getProjectName());
        }
        if (qryParam.getPipelineId() != null) {
            map.put("pipelineId", qryParam.getPipelineId());
        }
        if (StringUtils.isNotBlank(qryParam.getCreator())) {
            map.put("createUser", qryParam.getCreator());
        }
        map.put("deleted", 0);
        return map;
    }

    public Integer getPodNums( Integer pipelineId) {
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", pipelineId.toString()).list().getItems();
        podList = podList.stream().filter(pod -> pod.getStatus().getContainerStatuses().stream().allMatch(containerStatus -> containerStatus.getState().getRunning() != null)).toList();
        return podList.size();
    }

    public List<String> getContainers(Integer projectId, Integer pipelineId) {
        List<String> containers = new ArrayList<>();
        List<Pod> podList = podClient.inAnyNamespace().withLabel("pipeline-id", pipelineId.toString()).list().getItems();
        if (podList == null || podList.isEmpty()) {
            return containers;
        }
        podList.get(0).getStatus().getContainerStatuses().forEach(containerStatus -> {
            if (containerStatus.getName().equals(projectId.toString() + "-0-" + pipelineId.toString())) {
                containers.add("main");
            } else {
                containers.add(containerStatus.getName());
            }
        });
        if (containers.contains("main")) {
            //主容器放在第一位
            containers.remove("main");
            containers.add(0, "main");
        }
        return containers;
    }

    public PageData<ChaosTaskInfoVO> getTaskList(TaskQryParam qryParam) {
        if (!StringUtils.isEmpty(qryParam.getId())) {
            // 使用ID直接查询任务
            ChaosTask byId = taskDao.getById(qryParam.getId(), ChaosTask.class);
            if (byId != null) {
                // 转换单个任务到VO对象
                List<ChaosTaskInfoVO> chaosTaskInfoVOS = Collections.singletonList(convertToChaosTaskInfoVO(byId));
                return new PageData<>(chaosTaskInfoVOS, 1, 1, 1);
            } else {
                // 没有找到任务，返回空的分页数据
                return new PageData<>(Collections.emptyList(), 1, 0, 1);
            }
        } else {
            // 构建查询参数
            Map<String, Object> map = buildQueryParam(qryParam);
            // 查询任务列表
            PageData<ChaosTask> listByMap = taskDao.getListByMap(qryParam.buildPageData(), map);
            // 转换任务列表到VO对象列表
            List<ChaosTaskInfoVO> list = convertToChaosTaskInfoVOS(listByMap.getList());
            // 返回分页数据
            return new PageData<>(list, listByMap.getPage(), listByMap.getTotal(), listByMap.getPageSize());
        }
    }

    // 将ChaosTask转换为ChaosTaskInfoVO
    private ChaosTaskInfoVO convertToChaosTaskInfoVO(ChaosTask chaosTask) {
        return ChaosTaskInfoVO.builder().id(chaosTask.getId().toString())
                .experimentName(chaosTask.getExperimentName())
                .projectName(chaosTask.getProjectName())
                .type(chaosTask.getTaskType())
                .mode(chaosTask.getModeType())
                .status(chaosTask.getStatus())
                .creator(chaosTask.getCreateUser())
                .createTime(chaosTask.getCreateTime())
                .updateTime(chaosTask.getUpdateTime())
                .updateUser(chaosTask.getUpdateUser())
                .executedTimes(chaosTask.getExecutedTimes())
                .duration(chaosTask.getDuration())
                .build();
    }

    // 将ChaosTask列表转换为ChaosTaskInfoVO列表的辅助方法
    private List<ChaosTaskInfoVO> convertToChaosTaskInfoVOS(List<ChaosTask> chaosTasks) {
        return chaosTasks.stream()
                .map(this::convertToChaosTaskInfoVO)
                .collect(Collectors.toList());
    }

    public ChaosTaskDetailVO getChaosTaskDetail(String id) {
        ChaosTask chaosTaskById = getChaosTaskById(id);
        ChaosTaskDetailVO chaosTaskDetailVO = new ChaosTaskDetailVO(chaosTaskById);

        TaskExeLog exeLog = exeLogDao.getExeLogByTaskIdAndExecutedTimes(id, chaosTaskById.getExecutedTimes());
        if (Objects.nonNull(exeLog)) {
            chaosTaskDetailVO.setExecutor(exeLog.getExecutor());
            chaosTaskDetailVO.setStartTime(exeLog.getStartTime());
        }
        TaskEnum taskEnum = TaskEnum.fromType(chaosTaskById.getTaskType());
        Class poClass = taskEnum.getPoClass();
        chaosTaskDetailVO.setOperateParam(getOperateParam(chaosTaskById, poClass));
        return chaosTaskDetailVO;
    }

    private String getOperateParam(ChaosTask task, Class boClass) {
        String operateParam = "";
        Field[] fields = task.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(boClass)) {
                field.setAccessible(true);
                try {
                    Object fieldValue = field.get(task);
                    operateParam = gson.toJson(fieldValue);
                } catch (IllegalAccessException e) {
                    log.error("getOperateParam error:", e);
                }
            }
        }
        return operateParam;
    }


    public PageData<ChaosTaskReportVO> getTaskReportList(TaskQryParam qryParam) {
        PageData<ChaosTaskReport> chaosTaskPageData = qryParam.buildPageData();
        Map<String, Object> map = buildQueryParam(qryParam);
        if (StringUtils.isNotBlank(qryParam.getCreator())) {
            map.remove("createUser");
            map.put("taskCreator", qryParam.getCreator());
        }
        PageData<ChaosTaskReport> listByMap = taskReportDao.getListByMap(chaosTaskPageData, map);
        List<ChaosTaskReportVO> list = listByMap.getList().stream().map(report -> {
            ChaosTaskReportVO vo = new ChaosTaskReportVO();
            BeanUtils.copyProperties(report, vo);
            vo.setId(report.getId().toString());
            return vo;
        }).toList();
        PageData<ChaosTaskReportVO> res = new PageData<>();
        res.setList(list);
        res.setPageSize(listByMap.getPageSize());
        res.setPage(listByMap.getPage());
        res.setTotal(listByMap.getTotal());
        return res;
    }

    public ChaosTaskReportVO getChaosTaskReportDetail(String id) {
        ChaosTaskReport report = taskReportDao.getById(id, ChaosTaskReport.class);
        ChaosTaskReportVO vo = new ChaosTaskReportVO();
        BeanUtils.copyProperties(report, vo);
        vo.setId(report.getId().toString());
        ChaosTask chaosTask = taskDao.getById(report.getExperimentId(), ChaosTask.class);
        vo.setContainerNum(chaosTask.getContainerNum());
        vo.setInstanceInfoList(chaosTask.getInstanceAndIps().stream().map(r -> {
            InstanceInfoVO instanceInfoVO = new InstanceInfoVO();
            instanceInfoVO.setIp(r.getIp());
            instanceInfoVO.setContainerId(r.getContainerId());
            instanceInfoVO.setInstanceUid(r.getInstanceUid());
            return instanceInfoVO;
        }).toList());

        List<ChaosTaskLog> logList = Lists.newArrayList();
        // 初始化的也得查出来。
        PageData<ChaosTaskLog> pageData = new PageData<>();
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", report.getExperimentId());
        map.put("executedTimes", 0);
        PageData<ChaosTaskLog> initLogData = taskLogDao.getListByMap(pageData, map);
        logList.addAll(initLogData.getList());

        map.put("taskId", report.getExperimentId());
        map.put("executedTimes", report.getExecutedTimes());
        PageData<ChaosTaskLog> logData = taskLogDao.getListByMap(pageData, map);
        logList.addAll(logData.getList());
        List<ChaosTaskLog> list = logList.stream().sorted(Comparator.comparing(ChaosTaskLog::getCreateTime)).toList();
        Long startTime = 0L;
        Long endTime = 0L;

        for (ChaosTaskLog r : list) {
            if (r.getStatus().equals(StatusEnum.actioning.type())) {
                startTime = r.getCreateTime();
            }
            if (r.getStatus().equals(StatusEnum.recovered.type())) {
                endTime = r.getCreateTime();
            }
        }


        if (startTime != null && endTime != null) {
            vo.setExecuteTime(endTime - startTime);
        }

        TaskEnum taskEnum = TaskEnum.fromType(chaosTask.getTaskType());
        vo.setOperateParam(getOperateParam(chaosTask, taskEnum.getPoClass()));

        List<ChaosTaskLogVO> stepList = list.stream().map(r -> ChaosTaskLogVO.builder()
                .logId(r.getId().toString())
                .operator(r.getUpdateUser())
                .createTime(r.getCreateTime())
                .status(r.getStatus())
                .build()).toList();
        vo.setExeStep(stepList);

        return vo;
    }

    public String deleteTask(String id) {
        ChaosTask byId = taskDao.getById(id, ChaosTask.class);
        if (Objects.isNull(byId)) {
            throw new RuntimeException("task not found");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("deleted", 1);
        taskDao.update(byId.getId(), map);
        return byId.getId().toString();
    }

    public String deleteReport(String id) {
        ChaosTaskReport byId = taskReportDao.getById(id, ChaosTaskReport.class);
        if (Objects.isNull(byId)) {
            throw new RuntimeException("report not found");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("deleted", 1);
        taskReportDao.update(byId.getId(), map, ChaosTaskReport.class);
        return byId.getId().toString();
    }

    public String updateReport(String id,Map<String, Object> map) {
        ChaosTaskReport byId = taskReportDao.getById(id, ChaosTaskReport.class);
        if (Objects.isNull(byId)) {
            throw new RuntimeException("report not found");
        }
        taskReportDao.update(byId.getId(), map, ChaosTaskReport.class);
        return byId.getId().toString();
    }

    public String insertExeLog(ChaosTask task, String account) {
        TaskExeLog log = new TaskExeLog();
        log.setExecutor(account);
        log.setExperimentTimes(task.getExecutedTimes());
        log.setExperimentName(task.getExperimentName());
        log.setTaskId(task.getId().toString());
        log.setDuration(task.getDuration());
        log.setStartTime(System.currentTimeMillis());
        return exeLogDao.insert(log).toString();
    }
}
