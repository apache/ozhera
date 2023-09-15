package com.xiaomi.youpin.prometheus.agent.service.alarmContact;

import com.xiaomi.youpin.prometheus.agent.result.alertManager.AlertManagerFireResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zhangxiaowei6
 * @Date 2023/9/13 17:24
 */
//ding ding alert
@Slf4j
@Component
public class DingAlertContact extends BaseAlertContact {
    @Override
    public void Reach(AlertManagerFireResult fireResult) {

    }
}
