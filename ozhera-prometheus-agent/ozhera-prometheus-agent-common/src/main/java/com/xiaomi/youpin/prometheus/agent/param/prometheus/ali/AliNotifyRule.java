package com.xiaomi.youpin.prometheus.agent.param.prometheus.ali;

import lombok.Data;

import java.util.List;

/**
 * @author zhangxiaowei6
 * @Date 2024/1/15 11:04
 */
@Data
public class AliNotifyRule {
    private String notifyStartTime;
    private String notifyEndTime;
    private List<String> notifyChannels;
    private List<AliNotifyObjects> notifyObjects;
}
