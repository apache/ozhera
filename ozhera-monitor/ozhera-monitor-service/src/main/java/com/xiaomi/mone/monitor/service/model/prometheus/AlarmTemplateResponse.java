package com.xiaomi.mone.monitor.service.model.prometheus;

import com.xiaomi.mone.monitor.dao.model.AppAlarmRule;
import com.xiaomi.mone.monitor.dao.model.AppAlarmRuleTemplate;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2021/9/14 7:36 下午
 */
@Data
public class AlarmTemplateResponse implements Serializable {

    private AppAlarmRuleTemplate template;

    private List<AppAlarmRule> alarmRules;
}
