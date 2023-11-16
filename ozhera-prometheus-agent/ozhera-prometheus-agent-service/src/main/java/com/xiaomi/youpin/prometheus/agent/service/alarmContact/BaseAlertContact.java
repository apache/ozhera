package com.xiaomi.youpin.prometheus.agent.service.alarmContact;

import com.xiaomi.youpin.prometheus.agent.result.alertManager.AlertManagerFireResult;
import com.xiaomi.youpin.prometheus.agent.result.alertManager.Alerts;
import com.xiaomi.youpin.prometheus.agent.util.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public abstract class BaseAlertContact {

    Map<String, String> NAME_MAP = new HashMap<String, String>() {
        {
            put("application", "应用");

            put("calert", "报警中文名称");

            put("send_interval", "发送间隔");

            put("container", "容器");

            put("namespace", "命名空间");

            put("restartCounts", "重启次数");

            put("alert_key", "关键词");

            put("startTime", "开始时间");
        }
    };
    String[] ALERT_INVISIBLE_LIST = new String[]{"system", "exceptViewLables", "app_iam_id", "metrics_flag", "group_key", "job", "image"};

    void Reach(AlertManagerFireResult fireResult) {

    }

    void filterName(Map<String, Object> map) {
        List<Map.Entry<String, Object>> entries = new ArrayList<>(map.entrySet());
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            for (String s : ALERT_INVISIBLE_LIST) {
                if (StringUtils.containsIgnoreCase(key, s)) {
                    map.remove(key);
                }
            }
        }
    }

    Map<String, Object> transferNames(Map<String, Object> map) {
        List<Map.Entry<String, Object>> entries = new ArrayList<>(map.entrySet());
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            if (NAME_MAP.containsKey(key)) {
                Object value = entry.getValue();
                String newKey = NAME_MAP.get(key);
                map.remove(key);
                map.put(newKey, value);
            }
        }
        return map;
    }

    String GenerateAlarmUrl(String prefix, Alerts alerts) {
        String startsAt = alerts.getStartsAt();
        //Millisecond timestamp
        long alertTime = DateUtil.ISO8601UTCTOTimeStamp(startsAt);
        //The alert jump url starts 10 minutes before the alarm time and the end time is 10 minutes after the alarm time
        String startTime = String.valueOf(alertTime - (10 * 60 * 1000));
        String endTime = String.valueOf(alertTime + (10 * 60 * 1000));
        StringBuilder sb = new StringBuilder();
        String ip = alerts.getLabels().getIp() == null ? "ip" : alerts.getLabels().getIp();
        String serverIp = alerts.getLabels().getServerIp() == null ? "serverIp" : alerts.getLabels().getServerIp();
        String pod = alerts.getLabels().getPod() == null ? "pod" : alerts.getLabels().getPod();
        String serverEnv = alerts.getLabels().getServerEnv() == null ? "serverEnv" : alerts.getLabels().getServerEnv();
        sb.append(prefix).append("&var-Node=").append(ip).append("&ip=").append(serverIp).append("&var-pod=").append(pod)
                .append("&var-instance=").append(serverIp).append("&serverEnv=").append(serverEnv).append("&heraEnv=")
                .append(serverEnv).append("&startTime=").append(startTime).append("&endTime=").append(endTime).append("&from=")
                .append(startTime).append("&to=").append(endTime).append("&orgId=1").append("&refresh=10s");
        return sb.toString();
    }
}
