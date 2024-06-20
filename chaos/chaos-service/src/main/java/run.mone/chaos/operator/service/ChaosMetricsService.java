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

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xiaomi.youpin.docean.anno.Service;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import run.mone.chaos.operator.common.Config;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.ChaosTaskLog;
import run.mone.chaos.operator.dao.domain.ChaosTaskReport;
import run.mone.chaos.operator.dao.domain.InstanceUidAndIP;
import run.mone.chaos.operator.dao.domain.TaskExeLog;
import run.mone.chaos.operator.dao.impl.TaskExeLogDao;
import run.mone.chaos.operator.vo.PodLogUrlVO;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author zhangxiaowei6
 * @Date 2024/4/17 10:43
 */
@Slf4j
@Service
public class ChaosMetricsService {

    private final String ERROR_URL = "xxx";


    @Resource
    private ChaosTaskService chaosTaskService;
    @Resource
    private TaskExeLogDao exeLogDao;
    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(20))
            .readTimeout(Duration.ofSeconds(20))
            .build();


    public Map<String, String> getGoingGrafanaUrl(String id, int count, int status) {
        Map<String, String> res = new HashMap<>();
        try {
            ChaosTask task = chaosTaskService.getChaosTaskById(id);
            List<ChaosTaskLog> chaosTaskLogByTaskIdAndExecutedTimesAndStatus = chaosTaskService.getChaosTaskLogByTaskIdAndExecutedTimesAndStatus(count, id, status);
            if (chaosTaskLogByTaskIdAndExecutedTimesAndStatus == null || chaosTaskLogByTaskIdAndExecutedTimesAndStatus.isEmpty()) {
                log.info("Not found task log id:" + id + " count:" + count);
                return new HashMap<>();
            }
            ChaosTaskLog taskLog = chaosTaskLogByTaskIdAndExecutedTimesAndStatus.getFirst();
            if (taskLog.getInstanceUidAndIPList().isEmpty()) {
                log.info("No affected instances found");
            }

            // 获取报告信息 ，记录时间
            Stopwatch sw = Stopwatch.createStarted();
            List<ChaosTaskReport> chaosTaskReportByTaskIdAndExecutedTimes = chaosTaskService.getChaosTaskReportByTaskIdAndExecutedTimes(count, id);
            if (chaosTaskReportByTaskIdAndExecutedTimes == null || chaosTaskReportByTaskIdAndExecutedTimes.isEmpty()) {
                // 执行中没有report，直接生成
                res.put("grafanaUrl", packageGrafanaUrl(task, taskLog, null, true));
            } else {
                // 执行中已经有report，直接用
                ChaosTaskReport taskReport = chaosTaskReportByTaskIdAndExecutedTimes.getFirst();
                // 如果报告里有grafana链接，直接用，否则生成一遍
                if (taskReport.getMonitorUrl() != null && !taskReport.getMonitorUrl().isEmpty()) {
                    log.info("Found task reportId:{} grafanaUrl: {}", taskReport.getId(), taskReport.getMonitorUrl());
                    res.put("grafanaUrl", taskReport.getMonitorUrl());
                } else {
                    // 生成grafana链接
                    log.info("Not found task reportId:{} ,and begin generate", taskReport.getId());
                    res.put("grafanaUrl", packageGrafanaUrl(task, taskLog, taskReport, false));
                }
            }
            log.info("packageGrafanaUrl cost time:{}", sw.elapsed(TimeUnit.MILLISECONDS));

            res.put("heraUrl", packageHeraUrl(task, taskLog));
            return res;
        } catch (Exception e) {
            log.error("getGoingGrafanaUrl error", e);
            return new HashMap<>();
        }
    }

    private String getGrafanaDomain() {
        return Config.ins().get("grafana.domain", "https://grafana-mione.test.mi.com");
    }

    private String getLogUrl() {
        return Config.ins().get("dashLog.domain", "http://mionetty.systech.test.b2c.srv/kubelogs");
    }

    private List<Pod> getPodList(Integer projectId, Integer pipLineId) {
        Map<String, String> searchMap = new HashMap<>();
        searchMap.put("project-id", projectId.toString());
        searchMap.put("pipeline-id", pipLineId.toString());
        return podClient.inAnyNamespace().withLabels(searchMap).list().getItems();
    }

    private String packageGrafanaUrl(ChaosTask task, ChaosTaskLog taskLog, ChaosTaskReport taskReport, boolean isExecuteing) {
        // 只生成一遍，后续存入db
        StringBuilder sb = new StringBuilder();
        Integer projectId = task.getProjectId();
        List<Pod> podList = getPodList(projectId, task.getPipelineId());

        // 获取应用程序名称
        String originApplication = podList.getFirst().getMetadata().getLabels().get("mione-name");
        String application = projectId.toString() + "_" + originApplication.replaceAll("-", "_");

        // 根据任务模式选择不同的IP列表和节点IP列表
        List<String> ipList;
        List<String> nodeIpList;
        List<String> podNameList;
        //if (task.getModeType().equals(3)) {

        // 变量定为当时受影响的ip
        ipList = taskLog.getInstanceUidAndIPList().stream().map(InstanceUidAndIP::getIp).collect(Collectors.toList());
        nodeIpList = podList.stream().filter(pod -> ipList.contains(pod.getStatus().getPodIP()))
                .map(pod -> pod.getStatus().getHostIP()).collect(Collectors.toList());
        podNameList = podList.stream().filter(pod -> ipList.contains(pod.getStatus().getPodIP()))
                .map(pod -> pod.getMetadata().getName()).collect(Collectors.toList());

        if (task.getTaskType() == TaskEnum.pod.type()) {
            //如果是pod类型实验，由于有重启，ip会变，把当前所有实例的ip都加进列表
            ipList.addAll(podList.stream().map(pod -> pod.getStatus().getPodIP()).toList());
            nodeIpList.addAll(podList.stream().map(pod -> pod.getStatus().getHostIP()).toList());
            podNameList.addAll(podList.stream().map(pod -> pod.getMetadata().getName()).toList());
        }
       /* } else {
            ipList = podList.stream().map(pod -> pod.getStatus().getPodIP()).toList();
            nodeIpList = podList.stream().map(pod -> pod.getStatus().getHostIP()).toList();
            podNameList = podList.stream().map(pod -> pod.getMetadata().getName()).toList();
        }*/

        // 开始时间为实验log里的时间，log里的时间标识为第几次实验的时间
        Long createTime = taskLog.getCreateTime();
        log.info("ChaosMetricsService.packageUrl ipList:{},application:{}", ipList, application);

        TaskExeLog exeLog = exeLogDao.getExeLogByTaskIdAndExecutedTimes(task.getId().toString(), taskLog.getExecutedTimes());
        Long duration = task.getDuration();
        if (Objects.nonNull(exeLog)) {
            duration = exeLog.getDuration();
        }

        // 构建URL
        sb.append(getGrafanaDomain())
                .append(getUrlSuffix(task.getTaskType()))
                .append("&")
                .append(getUlrTimeParam(duration, createTime))
                .append("&var-application=")
                .append(application)
                .append("&refresh=15s&theme=light&kiosk")
                .append("&var-env=")
                .append(task.getPipelineId());

        Set<String> ipSet = new HashSet<>(ipList);
        Set<String> nodeIpSet = new HashSet<>(nodeIpList);
        Set<String> podNameSet = new HashSet<>(podNameList);
        appendUrlParams(sb, "var-podIp=", ipSet);
        appendUrlParams(sb, "var-Node=", nodeIpSet);
        appendUrlParams(sb, "var-pod=", podNameSet);

        if (!isExecuteing) {
            // 存入报告db，后续从db拿，防止后续看报告时，获取的pod等信息已经失效了(例如pod销毁了，就不能从历史的podip找到其nodeip了)
            Map<String, Object> updateMap = new HashMap<>(1);
            updateMap.put("monitorUrl", sb.toString());
            String updateRes = chaosTaskService.updateReport(String.valueOf(taskReport.getId()), updateMap);
            log.info("ChaosMetricsService.packageUrl monitorUrl insert to report updateRes:{}", updateRes);
        }
        return sb.toString();
    }

    private String packageHeraUrl(ChaosTask task, ChaosTaskLog taskLog) {
        Integer projectId = task.getProjectId();
        Integer pipelineId = task.getPipelineId();
        String projectName = task.getProjectName();
        long createTime = taskLog.getCreateTime();
        long duration = task.getDuration();
        long endTime = createTime + duration + (60 * 1000 * 5);

        // 获取 hera 相关配置
        String heraApiDomain = Config.ins().get("hera.api.domain", "http://mifaas.systech.test.b2c.srv/");
        String heraApiPath = Config.ins().get("hera.api.serverEnvName.path", "hera-api/hera-api-mimeter/mimeter/getServerEnvName");
        String heraDomain = Config.ins().get("hera.domain", "http://hera.be.test.mi.com/project-target-monitor/application/dash-board");

        // 获取 Pod 列表
        List<Pod> podList = getPodList(projectId, pipelineId);
        String originApplication = podList.getFirst().getMetadata().getLabels().get("mione-name");
        String application = projectId + "_" + originApplication.replaceAll("-", "_");

        // 构建请求 Hera API 的 URL，并获取 serverEnvName
        String requestHeraApiUrl = String.format("%s%s?application=%s&serverEnvId=%d", heraApiDomain, heraApiPath, application, pipelineId);

        Stopwatch stopwatch = Stopwatch.createStarted();
        String serverEnvName = getServerEnvName(requestHeraApiUrl);
        log.info("getServerEnvName cost time:{}", stopwatch.stop());

        // 构建 Hera 页面的 URL
        return String.format("%s?id=%d&name=%s&start_time=%d&end_time=%d&serverEnv=%s", heraDomain, projectId, projectName, createTime, endTime, serverEnvName);
    }


    private void appendUrlParams(StringBuilder sb, String paramName, Set<String> paramValues) {
        paramValues.forEach(value -> sb.append("&").append(paramName).append(value));
    }


    private String getUrlSuffix(int taskType) {
        String typeName = TaskEnum.fromType(taskType).typeName();
        return Config.ins().get("grafana." + typeName + ".url", ERROR_URL);
    }

    // duration 毫秒时间戳
    private String getUlrTimeParam(long duration, long createTime) {
        //开始时间往前5min
        long startTime = createTime - (60 * 1000 * 5);
        // 结束时间往后加1min再加duration
        long endTime = createTime + duration + (60 * 1000);
        return "from=" + startTime + "&to=" + endTime;
    }


    public List<PodLogUrlVO> getTaskLogUrl(String id) {
        ChaosTask task = chaosTaskService.getChaosTaskById(id);
        List<Pod> podList = getPodList(task.getProjectId(), task.getPipelineId());
        List<String> ipList = task.getInstanceAndIps().stream().map(InstanceUidAndIP::getIp).toList();
        return podList.stream().filter(pod -> ipList.contains(pod.getStatus().getPodIP())).map(i -> {
            PodLogUrlVO vo = new PodLogUrlVO();
            vo.setIp(i.getStatus().getPodIP());
            vo.setPodName(i.getMetadata().getName());
            vo.setLogUrl(buildLogUrl(i.getMetadata().getNamespace(), i.getMetadata().getName(), task.getProjectId(), task.getPipelineId()));
            return vo;
        }).toList();
    }

    public String getChaosServiceAvailabilityUrl() {
        String grafanaDomain = getGrafanaDomain();
        String path = Config.ins().get("grafana.chaos.all.url", "/d/mone_chaos_all/mone-chaos-all?orgId=1");
        return grafanaDomain + path + "&refresh=15s&theme=light";
    }

    private String buildLogUrl(String namespace, String podName, Integer projectId, Integer pipLineId) {
        return getLogUrl() + String.format("/%s/%s/%s-0-%s/", namespace, podName, projectId, pipLineId);
    }

    private String getServerEnvName(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                JsonObject jsonObject = new Gson().fromJson(responseBody, JsonObject.class);
                String data = jsonObject.get("data").getAsString();
                if (data == null || data.isEmpty()) {
                    log.info("getServerEnvName get data null url: {}, res: {}", url, responseBody);
                    return null;
                }
                return data;
            } else {
                log.info("getServerEnvName not successful url : {}", url);
                return null;
            }
        } catch (Exception e) {
            log.error("getServerEnvName error:", e);
            return null;
        }
    }

}
