package com.xiaomi.mone.monitor.service.prometheus;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.xiaomi.mone.monitor.bo.AlarmRuleMetricType;
import com.xiaomi.mone.monitor.bo.ResourceUsageMetrics;
import com.xiaomi.mone.monitor.dao.model.AppAlarmRule;
import com.xiaomi.mone.monitor.dao.model.AppMonitor;
import com.xiaomi.mone.monitor.pojo.AlarmPresetMetricsPOJO;
import com.xiaomi.mone.monitor.pojo.ReqErrorMetricsPOJO;
import com.xiaomi.mone.monitor.pojo.ReqSlowMetricsPOJO;
import com.xiaomi.mone.monitor.result.ErrorCode;
import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.alertmanager.AlarmExprService;
import com.xiaomi.mone.monitor.service.alertmanager.AlertServiceAdapt;
import com.xiaomi.mone.monitor.service.api.*;
import com.xiaomi.mone.monitor.service.helper.AlertUrlHelper;
import com.xiaomi.mone.monitor.service.model.PageData;
import com.xiaomi.mone.monitor.service.model.prometheus.AlarmRuleData;
import com.xiaomi.mone.monitor.service.model.prometheus.Metric;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gaoxihui
 * @date 2021/9/5 5:24 下午
 */
@Slf4j
@Service
public class AlarmService {

    @NacosValue(value = "${prometheus.alarm.env:staging}",autoRefreshed = true)
    private String prometheusAlarmEnv;

    @Value("${server.type}")
    private String serverType;

    @NacosValue(value = "${rule.evaluation.interval:30}",autoRefreshed = true)
    private Integer evaluationInterval;

    @NacosValue(value = "${rule.evaluation.duration:30}",autoRefreshed = true)
    private Integer evaluationDuration;

    @NacosValue(value = "${rule.evaluation.unit:s}",autoRefreshed = true)
    private String evaluationUnit;

    @Autowired
    PrometheusService prometheusService;

    @Autowired
    AlertServiceAdapt alertServiceAdapt;

    @Autowired
    private AlertUrlHelper alertUrlHelper;

    @Autowired
    private AlarmPresetMetricsService alarmPresetMetricsService;

    @Autowired
    private ReqErrorMetricsService reqErrorMetricsService;

    @Autowired
    private ReqSlowMetricsService reqSlowMetricsService;

    @Autowired
    private AlarmServiceExtension alarmServiceExtension;

    @Autowired
    private AlarmExprService alarmExprService;

    @Autowired
    TeslaService teslaService;

    public String getExpr(AppAlarmRule rule,String scrapeIntervel,AlarmRuleData ruleData, AppMonitor app){
        return alarmExprService.getExpr(rule,scrapeIntervel,ruleData,app);
    }

    @Value("${server.type}")
    private String env;

    public List<String> getInstanceIpList(Integer projectId, String projectName){

        List<Metric> metrics = listInstanceMetric(projectId, projectName);
        if(CollectionUtils.isEmpty(metrics)){
            log.error("getInstanceIps no data found! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }

        List<String> result = new ArrayList<>();
        for(Metric metric : metrics){
            result.add(metric.getIp());
        }

        return result;
    }

    public Map getEnvIpMapping(Integer projectId, String projectName){
        return alarmExprService.getEnvIpMapping(projectId,projectName);
    }


    private List<Metric> listInstanceMetric(Integer projectId,String projectName){
        projectName = projectName.replaceAll("-","_");

        StringBuilder builder = new StringBuilder();
        builder.append("process_uptime_seconds{application=\"")
                .append(projectId).append("_").append(projectName)
                .append("\"").append("}");
        Result<PageData> pageDataResult = prometheusService.queryByMetric(builder.toString());
        if(pageDataResult.getCode() != ErrorCode.success.getCode() || pageDataResult.getData() == null){
            log.error("queryByMetric error! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }

        List<Metric> list = (List<Metric>) pageDataResult.getData().getList();
        log.info("listInstanceMetric param projectName:{},result:{}",projectName,list.size());

        return list;
    }

    public List<String> getHttpClientServerDomain(Integer projectId, String projectName){

        List<Metric> metrics = listHttpMetric(projectId, projectName);
        if(CollectionUtils.isEmpty(metrics)){
            log.error("getHttpClientServerDomain no data found! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }

        List<String> result = new ArrayList<>();
        for(Metric metric : metrics){
            result.add(metric.getServiceName());
        }

        return result;
    }

    private List<Metric> listHttpMetric(Integer projectId,String projectName){
        projectName = projectName.replaceAll("-","_");

        StringBuilder builder = new StringBuilder();
        builder.append(serverType);
        builder.append("_jaeger_aopClientTotalMethodCount_total{application=\"")
                .append(projectId).append("_").append(projectName)
                .append("\"").append(",serviceName!=''}");
        Result<PageData> pageDataResult = prometheusService.queryByMetric(builder.toString());
        if(pageDataResult.getCode() != ErrorCode.success.getCode() || pageDataResult.getData() == null){
            log.error("queryByMetric error! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }

        List<Metric> list = (List<Metric>) pageDataResult.getData().getList();
        log.info("listHttpMetric param projectName:{},result:{}",projectName,list.size());

        return list;
    }

    private List<Metric> listContainerNameMetric(Integer projectId,String projectName){
        projectName = projectName.replaceAll("-","_");

        StringBuilder builder = new StringBuilder();
        builder.append("jvm_classes_loaded_classes{ containerName != '',application=\"")
                .append(projectId).append("_").append(projectName)
                .append("\"").append("}");
        Result<PageData> pageDataResult = prometheusService.queryByMetric(builder.toString());
        if(pageDataResult.getCode() != ErrorCode.success.getCode() || pageDataResult.getData() == null){
            log.error("listContainerNameMetric error! projectId :{},projectName:{}",projectId,projectName);
            return null;
        }

        List<Metric> list = (List<Metric>) pageDataResult.getData().getList();
        log.info("listContainerNameMetric param projectName:{},result:{}",projectName,list.size());

        return list;
    }

    public List<String> listContainerName(Integer projectId,String projectName){

        List<Metric> metrics = listContainerNameMetric(projectId, projectName);
        if(CollectionUtils.isEmpty(metrics)){
            return Lists.newArrayList();
        }
        return metrics.stream().map(t -> t.getContainerName()).distinct().collect(Collectors.toList());
    }

    public Result addRule(AppMonitor app, AppAlarmRule rule, String user, AlarmRuleData ruleData){



        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("alert", rule.getAlert());


        jsonObject.addProperty("cname", rule.getCname());


        jsonObject.addProperty("for", rule.getForTime());
        jsonObject.addProperty("forTime", rule.getForTime());


        StringBuilder title = new StringBuilder().append(app.getProjectName());
        AlarmPresetMetricsPOJO metrics = alarmPresetMetricsService.getByCode(rule.getAlert());
        if (metrics != null) {
            title.append("&").append(metrics.getMessage());
        } else {
            //check tesla metrics
            teslaService.checkTeslaMetrics(title, rule.getAlert());
        }
        JsonObject jsonSummary = new JsonObject();
        jsonSummary.addProperty("title", title.toString());
        if (StringUtils.isNotBlank(rule.getRemark())) {
            jsonSummary.addProperty("summary", rule.getRemark());
        }
        if (StringUtils.isNotBlank(ruleData.getAlarmCallbackUrl())) {
            jsonSummary.addProperty("callback_url", ruleData.getAlarmCallbackUrl());
        }

        jsonObject.add("annotations", jsonSummary);

        Result<String> groupResult = alarmServiceExtension.getGroup(rule.getIamId(), user);
        if(!groupResult.isSuccess()){
            return groupResult;
        }

        jsonObject.addProperty("group", groupResult.getData());

        jsonObject.addProperty("priority", rule.getPriority());


        /**
         * env
         */
        JsonArray envArray = new JsonArray();
        envArray.add(rule.getEnv());
        jsonObject.add("env", envArray);

        /**
         * labels
         */
        JsonObject labels = new JsonObject();
        labels.addProperty("exceptViewLables","detailRedirectUrl.paramType");
        if(StringUtils.isNotBlank(ruleData.getAlarmDetailUrl())){
            labels.addProperty("detailRedirectUrl",ruleData.getAlarmDetailUrl());
            labels.addProperty("paramType","customerPromql");
        }

        alertUrlHelper.buildDetailRedirectUrl(user, app, rule.getAlert(), jsonSummary, labels);

        labels.addProperty("send_interval",rule.getSendInterval());
        labels.addProperty("app_iam_id",String.valueOf(rule.getIamId()));
        labels.addProperty("project_id",String.valueOf(rule.getProjectId()));
        labels.addProperty("project_name",app.getProjectName());
        if (StringUtils.isNotBlank(rule.getAlert())) {
            labels.addProperty("alert_key",rule.getAlert());
        }
        if (StringUtils.isNotBlank(rule.getOp())) {
            labels.addProperty("alert_op",rule.getOp());
        }
        //报警阈值
        if(rule.getMetricType() == AlarmRuleMetricType.customer_promql.getCode()){

            String ruleExpr = ruleData.getExpr();
            Set set = new HashSet();
            set.add(ruleExpr.lastIndexOf(">="));
            set.add(ruleExpr.lastIndexOf("<="));
            set.add(ruleExpr.lastIndexOf("!="));
            set.add(ruleExpr.lastIndexOf(">"));
            set.add(ruleExpr.lastIndexOf("<"));
            set.add(ruleExpr.lastIndexOf("="));

            List<Integer> indexSet = (List) set.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());

            int a = CollectionUtils.isEmpty(indexSet) ? -1 : indexSet.get(0);
            log.info("add customer_promql ruleExpr :{},a:{}",ruleExpr,a);

            String value = "0.0";
            if (a > 0) {
                try {
                    value = ruleExpr.substring(a + 1).trim();
                } catch (NumberFormatException e) {
                    log.error(e.getMessage() + "ruleExpr : {} ; a : {}", ruleExpr, a, e);
                }
            }
            labels.addProperty("alert_value",value);

        }else if (rule.getValue() != null) {
            labels.addProperty("alert_value",rule.getValue().toString());
        }
        // labels.addProperty("data_count",rule.getDataCount().toString());
        if (metrics != null) {
            labels.addProperty("calert",metrics.getMessage());
            labels.addProperty("group_key",metrics.getGroupKey().getCode());
        } else {
            labels.addProperty("calert",rule.getAlert());
        }
        ReqErrorMetricsPOJO errMetrics = reqErrorMetricsService.getErrorMetricsByMetrics(rule.getAlert());
        if (errMetrics != null) {
            //error metric flag
            labels.addProperty("metrics_flag","1");
            labels.addProperty("metrics",errMetrics.getCode());
        }
        ReqSlowMetricsPOJO slowMetrics = reqSlowMetricsService.getSlowMetricsByMetric(rule.getAlert());
        if (slowMetrics != null) {
            //slow query metric flag
            labels.addProperty("metrics_flag","2");
            labels.addProperty("metrics",slowMetrics.getCode());
        }

        ResourceUsageMetrics errorMetricsByMetrics = ResourceUsageMetrics.getErrorMetricsByMetrics(rule.getAlert());
        if (errorMetricsByMetrics != null) {
            //resource usage flag
            labels.addProperty("metrics_flag",errorMetricsByMetrics.getMetricsFlag());
            labels.addProperty("metrics",errorMetricsByMetrics.getCode());
        }
        jsonObject.add("labels", labels);


        if(rule.getMetricType().equals(AlarmRuleMetricType.preset.getCode())){
            String evaluationIntervalS = evaluationDuration + evaluationUnit;
            String expr = getExpr(rule,evaluationIntervalS,ruleData, app);
            log.info("presetMetric expr===========" + expr);
            if(StringUtils.isBlank(expr)){
                log.error("getExpr error!rule:{},projectName:{}",rule.toString(),app.getProjectName());
                return Result.fail(ErrorCode.unknownError);
            }
            jsonObject.addProperty("expr", expr);

            rule.setExpr(expr);

        }else if(rule.getMetricType().equals(AlarmRuleMetricType.customer_promql.getCode())){
            log.info("customize expr:projectId:{},projectName:{},expr:{}",app.getProjectId(),app.getProjectName(),ruleData.getExpr());
            jsonObject.addProperty("expr", ruleData.getExpr());
        }

        /**
         * alert team
         */
        String alertTeamJson = rule.getAlertTeam();

        List<String> alertMembers = ruleData.getAlertMembers();

        if(StringUtils.isBlank(alertTeamJson) && CollectionUtils.isEmpty(alertMembers)){
            log.error("AlarmService.addRule error! invalid alarmTeam and alertMembers param!");
            return Result.fail(ErrorCode.ALERT_TEAM_AND_ALERT_MEMBERS_BOTH_EMPTY);
        }

        if(StringUtils.isNotBlank(alertTeamJson)){
            JsonArray array = new Gson().fromJson(alertTeamJson, JsonArray.class);
            jsonObject.add("alert_team", array);
        }

        if(!CollectionUtils.isEmpty(alertMembers)){
            JsonArray array = new Gson().fromJson(JSON.toJSONString(alertMembers), JsonArray.class);
            jsonObject.add("alert_member", array);
        }

        if(!CollectionUtils.isEmpty(ruleData.getAtMembers())){
            JsonArray array = new Gson().fromJson(JSON.toJSONString(ruleData.getAtMembers()), JsonArray.class);
            jsonObject.add("alert_at_people", array);
        }

        return alertServiceAdapt.addRule(jsonObject,String.valueOf(rule.getIamId()),user);
    }

    public Result editRule(AppAlarmRule rule,AlarmRuleData ruleData,AppMonitor app,String user){

        /**
         * modifiable field：
         * cname
         * expr
         * for
         * labels
         * annotations
         * group
         * priority
         * env
         * alert_team
         * alert_member
         */

        JsonObject jsonObject = new JsonObject();


        /**
         * cname
         */
        if(StringUtils.isNotBlank(rule.getCname())){
            jsonObject.addProperty("cname", rule.getCname());
        }

        /**
         * for
         */
        if(StringUtils.isNotBlank(rule.getForTime())){
            jsonObject.addProperty("for", rule.getForTime());
            jsonObject.addProperty("forTime", rule.getForTime());
        }

        /**
         * annotations
         */
        StringBuilder title = new StringBuilder().append(app.getProjectName());
        AlarmPresetMetricsPOJO metrics = alarmPresetMetricsService.getByCode(rule.getAlert());
        if (metrics != null) {
            title.append("&").append(metrics.getMessage());
        } else {
            //check tesla metrics
            teslaService.checkTeslaMetrics(title, rule.getAlert());

        }
        JsonObject jsonSummary = new JsonObject();
        jsonSummary.addProperty("title", title.toString());
        if (StringUtils.isNotBlank(rule.getRemark())) {
            jsonSummary.addProperty("summary", rule.getRemark());
        }
        if (StringUtils.isNotBlank(ruleData.getAlarmCallbackUrl())) {
            jsonSummary.addProperty("callback_url", ruleData.getAlarmCallbackUrl());
        }

        jsonObject.add("annotations", jsonSummary);

        /**
         * priority
         */
        if(StringUtils.isNotBlank(rule.getPriority())){
            jsonObject.addProperty("priority", rule.getPriority());
        }


        /**
         * labels
         */
        JsonObject labels = new JsonObject();
        labels.addProperty("exceptViewLables","detailRedirectUrl.paramType");

        if(StringUtils.isNotBlank(ruleData.getAlarmDetailUrl())){
            labels.addProperty("detailRedirectUrl",ruleData.getAlarmDetailUrl());
            labels.addProperty("paramType","customerPromql");
        }

        alertUrlHelper.buildDetailRedirectUrl(user, app, rule.getAlert(), jsonSummary, labels);

        labels.addProperty("send_interval",rule.getSendInterval());
        labels.addProperty("app_iam_id",String.valueOf(rule.getIamId()));
        labels.addProperty("project_id",String.valueOf(rule.getProjectId()));
        labels.addProperty("project_name",app.getProjectName());
        if (StringUtils.isNotBlank(rule.getAlert())) {
            labels.addProperty("alert_key",rule.getAlert());
        }
        if (StringUtils.isNotBlank(rule.getOp())) {
            labels.addProperty("alert_op",rule.getOp());
        }

        if(rule.getMetricType() == AlarmRuleMetricType.customer_promql.getCode()){

            String ruleExpr = ruleData.getExpr();
            int a = ruleExpr.lastIndexOf(">") > 0 ? ruleExpr.lastIndexOf(">") :
                    ruleExpr.lastIndexOf("<") > 0 ? ruleExpr.lastIndexOf("<") :
                            ruleExpr.lastIndexOf("=") > 0 ? ruleExpr.lastIndexOf("=") :
                                    ruleExpr.lastIndexOf(">=") > 0 ? ruleExpr.lastIndexOf(">=") :
                                            ruleExpr.lastIndexOf("<=") > 0 ? ruleExpr.lastIndexOf("<=") :
                                                    ruleExpr.lastIndexOf("!=") > 0 ? ruleExpr.lastIndexOf("!=") :
                                                            -1;
            log.info("edit customer_promql ruleExpr :{},a:{}",ruleExpr,a);

            String value = "0.0";
            if (a > 0) {
                try {
                    value = ruleExpr.substring(a + 1).trim();
                } catch (NumberFormatException e) {
                    log.error(e.getMessage() + "ruleExpr : {} ; a : {}", ruleExpr, a, e);
                }
            }
            labels.addProperty("alert_value",value);

        }else if (rule.getValue() != null) {
            labels.addProperty("alert_value",rule.getValue().toString());
        }

        if (metrics != null) {
            labels.addProperty("calert",metrics.getMessage());
            labels.addProperty("group_key",metrics.getGroupKey().getCode());
        } else {
            labels.addProperty("calert",rule.getAlert());
        }

        ReqErrorMetricsPOJO errMetrics = reqErrorMetricsService.getErrorMetricsByMetrics(rule.getAlert());
        if (errMetrics != null) {
            //error metric flag
            labels.addProperty("metrics_flag","1");
            labels.addProperty("metrics",errMetrics.getCode());
        }
        ReqSlowMetricsPOJO slowMetrics = reqSlowMetricsService.getSlowMetricsByMetric(rule.getAlert());
        if (slowMetrics != null) {
            //slow query metric flag
            labels.addProperty("metrics_flag","2");
            labels.addProperty("metrics",slowMetrics.getCode());
        }

        ResourceUsageMetrics errorMetricsByMetrics = ResourceUsageMetrics.getErrorMetricsByMetrics(rule.getAlert());
        if (errorMetricsByMetrics != null) {
            //resource usage flag
            labels.addProperty("metrics_flag",errorMetricsByMetrics.getMetricsFlag());
            labels.addProperty("metrics",errorMetricsByMetrics.getCode());
        }
        jsonObject.add("labels", labels);


        /**
         * expr
         */
        if(rule.getMetricType().equals(AlarmRuleMetricType.preset.getCode())){
            String evaluationIntervalS = evaluationDuration + evaluationUnit;
            String expr = getExpr(rule,evaluationIntervalS,ruleData, app);
            log.info("presetMetric expr===========" + expr);
            if(StringUtils.isBlank(expr)){
                log.error("getExpr error!rule:{},projectName:{}",rule.toString(),app.getProjectName());
                return Result.fail(ErrorCode.unknownError);
            }
            jsonObject.addProperty("expr", expr);

            rule.setExpr(expr);

        }else if(rule.getMetricType().equals(AlarmRuleMetricType.customer_promql.getCode())){
            log.info("customer customize expr:projectId:{},projectName:{},expr:{}",app.getProjectId(),app.getProjectName(),ruleData.getExpr());
            jsonObject.addProperty("expr", ruleData.getExpr());
            rule.setExpr(ruleData.getExpr());
        }

        /**
         * alert team and alert_members
         */
        String alertTeamJson = rule.getAlertTeam();

        List<String> alertMembers = ruleData.getAlertMembers();

        if(StringUtils.isBlank(alertTeamJson) && CollectionUtils.isEmpty(alertMembers)){
            log.error("AlarmService.editRule error! invalid alarmTeam and alertMembers param!");
            return Result.fail(ErrorCode.ALERT_TEAM_AND_ALERT_MEMBERS_BOTH_EMPTY);
        }

        if(StringUtils.isNotBlank(alertTeamJson)){
            JsonArray array = new Gson().fromJson(alertTeamJson, JsonArray.class);
            jsonObject.add("alert_team", array);
        }


        JsonArray membersArray = new JsonArray();
        if(!CollectionUtils.isEmpty(alertMembers)){
            membersArray = new Gson().fromJson(JSON.toJSONString(alertMembers), JsonArray.class);
        }
        jsonObject.add("alert_member", membersArray);


        JsonArray atMembersArray = new JsonArray();
        if(!CollectionUtils.isEmpty(ruleData.getAtMembers())){
            atMembersArray = new Gson().fromJson(JSON.toJSONString(ruleData.getAtMembers()), JsonArray.class);
        }
        jsonObject.add("alert_at_people", atMembersArray);

        return alertServiceAdapt.editRule(rule.getAlarmId(),jsonObject,String.valueOf(rule.getIamId()),user);
    }

    public Result<JsonElement>  getAlarmRuleRemote(Integer alarmId,Integer iamId,String user){
        return alertServiceAdapt.getAlarmRuleRemote(alarmId,iamId,user);
    }

    public Result updateAlarm(Integer alarmId,Integer iamId,String user,String body){
        return alertServiceAdapt.updateAlarm(alarmId,iamId,user,body);
    }


    public Result deleteRule(Integer alarmId,Integer iamId, String user){
        return alertServiceAdapt.delRule(alarmId,String.valueOf(iamId),user);
    }

    public Result enabledRule(Integer alarmId,Integer pauseStatus,Integer iamId, String user){
        return alertServiceAdapt.enableRule(alarmId,pauseStatus,String.valueOf(iamId),user);
    }

    public Result<PageData> queryRuels(Integer iamId, String user, String alert, String cname, String env, String priority, String expr, Map<String,String> labels){


        JsonObject params = new JsonObject();

        if(!CollectionUtils.isEmpty(labels)){
            Set<Map.Entry<String, String>> set = labels.entrySet();
            JsonArray jsonLabels = new JsonArray();
            for(Map.Entry<String, String> entry : set){
                JsonObject jsonAlertTeam = new JsonObject();
                jsonAlertTeam.addProperty(entry.getKey(),entry.getValue());
                jsonLabels.add(jsonAlertTeam);
            }
            params.add("labels",jsonLabels);
        }

        if(StringUtils.isNotBlank(alert)){
            params.addProperty("alert",alert);
        }
        if(StringUtils.isNotBlank(cname)){
            params.addProperty("cname",cname);
        }
        if(StringUtils.isNotBlank(env)){
            params.addProperty("env",env);
        }
        if(StringUtils.isNotBlank(priority)){
            params.addProperty("priority",priority);
        }
        if(StringUtils.isNotBlank(expr)){
            params.addProperty("expr",expr);
        }

        return alertServiceAdapt.queryRuels(params,String.valueOf(iamId),user);
    }

    /**
     *
     * @param alarmGroup
     * @param iamId
     * @param user
     * @return
     * eg:
     * {
     *     "code":0,
     *     "message":"success",
     *     "data":{
     *         "id":1137
     *     }
     * }
     */
    public Result<JsonElement> addAlarmGroup(String alarmGroup,Integer iamId,String user){

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("group", alarmGroup);
        String evaluationIntervalS = evaluationInterval + evaluationUnit;
        jsonObject.addProperty("interval", evaluationIntervalS);

        return alertServiceAdapt.addAlarmGroup(jsonObject,String.valueOf(iamId),user);
    }

    public Result<JsonElement> searchAlarmGroup(String alarmGroup,Integer iamId,String user){
        return alertServiceAdapt.searchAlarmGroup(alarmGroup,String.valueOf(iamId),user);
    }

    public Result<PageData> searchAlertTeam(String name,String note,String manager,String oncallUser,String service,Integer iamId,String user,Integer page_no,Integer page_size){
        return alertServiceAdapt.searchAlertTeam(name,note,manager,oncallUser,service,iamId,user,page_no,page_size);
    }

    public Result<PageData> queryEvents(String user, Integer treeId, String alertLevel, Long startTime, Long endTime, Integer pageNo, Integer pageSize, JsonObject labels) {
        return alertServiceAdapt.queryEvents(user,treeId,alertLevel,startTime,endTime,pageNo,pageSize,labels);
    }

    public Result<PageData> queryLatestEvents(Set<Integer> treeIdSet, String alertStat, String alertLevel, Long startTime, Long endTime, Integer pageNo, Integer pageSize, JsonObject labels) {
        return alertServiceAdapt.queryLatestEvents(treeIdSet,alertStat,alertLevel,startTime,endTime,pageNo,pageSize,labels);
    }

    public Result<JsonObject> getEventById(String user, Integer treeId, String eventId) {
        return alertServiceAdapt.getEventById(user,treeId,eventId);
    }

    public Result<JsonObject> resolvedEvent(String user, Integer treeId, String alertName, String comment, Long startTime, Long endTime) {
        return alertServiceAdapt.resolvedEvent(user,treeId,alertName,comment,startTime,endTime);
    }

}
