package com.xiaomi.mone.monitor.service.alertmanager;

import com.xiaomi.mone.monitor.dao.model.AppAlarmRule;
import com.xiaomi.mone.monitor.dao.model.AppMonitor;
import com.xiaomi.mone.monitor.service.model.prometheus.AlarmRuleData;

import java.util.Map;

/**
 * @author gaoxihui
 * @date 2023/10/6 11:38 上午
 */
public interface AlarmExprService {

    public String getExpr(AppAlarmRule rule, String scrapeIntervel, AlarmRuleData ruleData, AppMonitor app);

    public String getContainerCpuResourceAlarmExpr(Integer projectId,String projectName,String op,double value,boolean isK8s,AlarmRuleData ruleData);

    public String getContainerMemReourceAlarmExpr(Integer projectId,String projectName,String op,double value,boolean isK8s,AlarmRuleData ruleData);

    public Map getEnvIpMapping(Integer projectId, String projectName);
}
