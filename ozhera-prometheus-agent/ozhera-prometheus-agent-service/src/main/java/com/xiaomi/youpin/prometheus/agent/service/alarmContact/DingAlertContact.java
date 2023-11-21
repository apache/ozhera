package com.xiaomi.youpin.prometheus.agent.service.alarmContact;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.xiaomi.youpin.prometheus.agent.Impl.RuleAlertDao;
import com.xiaomi.youpin.prometheus.agent.entity.RuleAlertEntity;
import com.xiaomi.youpin.prometheus.agent.result.alertManager.AlertManagerFireResult;
import com.xiaomi.youpin.prometheus.agent.result.alertManager.Alerts;
import com.xiaomi.youpin.prometheus.agent.result.alertManager.CommonLabels;
import com.xiaomi.youpin.prometheus.agent.result.alertManager.GroupLabels;
import com.xiaomi.youpin.prometheus.agent.service.DingDingService;
import com.xiaomi.youpin.prometheus.agent.service.FeishuService;
import com.xiaomi.youpin.prometheus.agent.util.DateUtil;
import com.xiaomi.youpin.prometheus.agent.util.FreeMarkerUtil;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author zhangxiaowei6
 * @Date 2023/9/13 17:24
 */

/**
 * @author zhangxiaowei6
 * @Date 2023/11/15 20:01
 */
//ding ding alert
@Slf4j
@Component
public class DingAlertContact extends BaseAlertContact {

    @Autowired
    RuleAlertDao dao;

    @Autowired
    private DingDingService dingDingService;

    @NacosValue(value = "${hera.alertmanager.url}", autoRefreshed = true)
    private String silenceUrl;

    public static final Gson gson = new Gson();

    public static final Random random = new Random();

    @Override
    public void Reach(AlertManagerFireResult fireResult) {
        GroupLabels groupLabels = fireResult.getGroupLabels();
        String alertName = groupLabels.getAlertname();
        log.info("SendAlert dingdingReach begin send AlertName :{}", alertName);
        fireResult.getAlerts().stream().forEach(alert -> {
            try {
                // query responsible person
                String[] principals = dao.GetRuleAlertAtPeople(alertName);
                if (principals == null) {
                    log.info("SendAlert principals null alertName:{}", alertName);
                    return;
                }
                log.info("SendAlert dingdingReach AlertName :{} , principals:{}", alertName,principals);
                RuleAlertEntity ruleAlertEntity = dao.GetRuleAlertByAlertName(alert.getLabels().getAlertname());
                int priority = ruleAlertEntity.getPriority();
                Map<String, Object> map = new HashMap<>();
                map.put("priority", "P" + String.valueOf(priority));
                map.put("title", fireResult.getCommonAnnotations().getTitle());
                String alertOp = alert.getLabels().getAlert_op();
                String alertValue = alert.getLabels().getAlert_value();
                if (alertOp == null || alertOp.isEmpty()) {
                    alertOp = "";
                    alertValue = "";
                }
                //Generate alarm jump url
                String generateAlarmJumpUrl = GenerateAlarmUrl(alert.getLabels().getDetailRedirectUrl(), alert);
                log.info("DingAlertContact.generateAlarmJumpUrl: {}",generateAlarmJumpUrl);
                map.put("alert_op", alertOp);
                map.put("alert_value", alertValue);
                map.put("application", alert.getLabels().getApplication());
                map.put("silence_url", silenceUrl);
                map.put("detailRedirectUrl",generateAlarmJumpUrl);
                map.put("startTime", DateUtil.ISO8601UTCTOCST(alert.getStartsAt()));
                CommonLabels commonLabels = fireResult.getCommonLabels();
                Class clazz = commonLabels.getClass();
                Field[] fields = clazz.getDeclaredFields();
                StringBuilder sb = new StringBuilder();
                for (Field field : fields) {
                    // set access rights
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    Object fieldValue = null;
                    try {
                        // convert fieldValue to String
                        fieldValue = field.get(commonLabels); // Get field value
                        if (fieldValue == null) {
                            continue;
                        }
                        map.put(fieldName, field.get(commonLabels));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                Map<String, Object> finalMap = transferNames(map);
                filterName(finalMap);
                finalMap.forEach(
                        (k, v) -> {
                            if (k.equals("detailRedirectUrl")) {
                                return;
                            }
                            sb.append("**").append(k).append("**").append(": ").append(v).append("\n");
                        });

                String content = sb.toString();
                finalMap.put("content", content);
                //begin constructive silent parameter
                String callbackTitle = "[" + priority + "][Hera] " + fireResult.getCommonAnnotations().getTitle() + alertOp + alertValue;
                StringBuilder silenceSb = new StringBuilder();
                String silencePrefix = silenceSb.append(alert.getLabels().getApplication()).append("||")
                        .append(alert.getLabels().getAlertname()).append("||").append(content).append("||")
                        .append(callbackTitle).append("||").toString();
                finalMap.put("silence2h", silencePrefix + "2h");
                finalMap.put("silence1d", silencePrefix + "1d");
                finalMap.put("silence3d", silencePrefix + "3d");
                String freeMarkerRes = FreeMarkerUtil.getContent("/dingding", "dingdingbasicCart.ftl", finalMap);
                int randomNumber = random.nextInt(1000);
                dingDingService.sendDingDing(freeMarkerRes, principals, alert.getLabels().getAlertname() +
                        "||" + System.currentTimeMillis() + randomNumber);
            } catch (Exception e) {
                log.error("SendAlert.feishuReach error:{}", e);
            }
        });

        log.info("SendAlert success AlertName:{}", alertName);
    }

    public void updateDingDingCard(String userId, String content, String expectedSilenceTime, String carBizId,String callbackTitle) {
        log.info("DingAlertContact.updateDingDingCard begin userId:{},content:{},expectedSilenceTime:{},carBizId:{}",
                userId, content, expectedSilenceTime, carBizId);
        Map<String, Object> finalMap = new HashMap<>();
        finalMap.put("content", content);
        finalMap.put("callbackTitle",callbackTitle);
        String nameByUserId = dingDingService.getNameByUserId(userId);
        finalMap.put("updateUser", "**" + "已由 <font color=common_blue1_color>" + nameByUserId +
                " </font>静默" + " <font color=common_red1_color>" + expectedSilenceTime + "</font>" + "**");
        try {
            String freeMarkerResUpdate = FreeMarkerUtil.getContent("/dingding", "dingdingbasicUpdateCart.ftl", finalMap);
            dingDingService.updateDingDingCard(freeMarkerResUpdate, carBizId);
        } catch (Exception e) {
            log.error("DingAlertContact.updateDingDingCard error:{}", e);
        }
    }
}
