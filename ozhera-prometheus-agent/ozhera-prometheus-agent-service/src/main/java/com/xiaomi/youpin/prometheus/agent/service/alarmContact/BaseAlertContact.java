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
package com.xiaomi.youpin.prometheus.agent.service.alarmContact;

import com.xiaomi.youpin.prometheus.agent.result.alertManager.AlertManagerFireResult;
import com.xiaomi.youpin.prometheus.agent.result.alertManager.Alerts;
import com.xiaomi.youpin.prometheus.agent.util.DateUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
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
        Alerts fixAlerts = StringNull2Null(alerts);
        log.info("BaseAlertContact.GenerateAlarmUrl after fix alerts:{}", fixAlerts);
        String startsAt = alerts.getStartsAt();
        //Millisecond timestamp
        long alertTime = DateUtil.ISO8601UTCTOTimeStamp(startsAt);
        //The alert jump url starts 10 minutes before the alarm time and the end time is 10 minutes after the alarm time
        String startTime = String.valueOf(alertTime - (10 * 60 * 1000));
        String endTime = String.valueOf(alertTime + (10 * 60 * 1000));
        StringBuilder sb = new StringBuilder();
        String ip = fixAlerts.getLabels().getIp() == null ? "ip" : fixAlerts.getLabels().getIp();
        String serverIp = fixAlerts.getLabels().getServerIp() == null ? "serverIp" : fixAlerts.getLabels().getServerIp();
        String pod = fixAlerts.getLabels().getPod() == null ? "pod" : fixAlerts.getLabels().getPod();
        String serverEnv = fixAlerts.getLabels().getServerEnv() == null ? "serverEnv" : fixAlerts.getLabels().getServerEnv();
        String metric = fixAlerts.getLabels().getMetrics() == null ? "metric" : fixAlerts.getLabels().getMetrics();
        String metric_flag = fixAlerts.getLabels().getMetrics_flag() == null ? "metric_flag" : fixAlerts.getLabels().getMetrics_flag();
        if (!"metric_flag".equals(metric_flag)) {
            metric_flag = activeTab.getMessageByCode(Integer.parseInt(metric_flag));
        }
        sb.append(prefix).append("&var-Node=").append(ip).append("&ip=").append(serverIp).append("&var-pod=").append(pod)
                .append("&var-instance=").append(serverIp).append("&serverEnv=").append(serverEnv).append("&heraEnv=")
                .append(serverEnv).append("&startTime=").append(startTime).append("&endTime=").append(endTime).append("&from=")
                .append(startTime).append("&to=").append(endTime).append("&metric=").append(metric).append("&activeTab=")
                .append(metric_flag).append("&orgId=1").append("&refresh=10s");
        return sb.toString();
    }


    enum activeTab {
        exception(1, "exception"),
        slowQuery(2, "slowQuery"),
        resource(4, "resource"),
        ;
        private int code;
        private String message;

        activeTab(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public static String getMessageByCode(int code) {
            for (activeTab tab : activeTab.values()) {
                if (tab.code == code) {
                    return tab.message;
                }
            }
            return null;
        }
    }

    private Alerts StringNull2Null(Alerts alerts) {
        // if the string is null, set it to null
        alerts.getLabels().setIp(Objects.equals(alerts.getLabels().getIp(), "null") ? null : alerts.getLabels().getIp());
        alerts.getLabels().setServerIp(Objects.equals(alerts.getLabels().getServerIp(), "null") ? null : alerts.getLabels().getServerIp());
        alerts.getLabels().setPod(Objects.equals(alerts.getLabels().getPod(), "null") ? null : alerts.getLabels().getPod());
        alerts.getLabels().setServerEnv(Objects.equals(alerts.getLabels().getServerEnv(), "null") ? null : alerts.getLabels().getServerEnv());
        alerts.getLabels().setMetrics(Objects.equals(alerts.getLabels().getMetrics(), "null") ? null : alerts.getLabels().getMetrics());
        alerts.getLabels().setMetrics_flag(Objects.equals(alerts.getLabels().getMetrics_flag(), "null") ? null : alerts.getLabels().getMetrics_flag());
        return alerts;
    }
}
