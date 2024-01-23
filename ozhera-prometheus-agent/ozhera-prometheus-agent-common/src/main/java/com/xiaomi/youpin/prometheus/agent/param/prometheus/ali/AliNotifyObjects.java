package com.xiaomi.youpin.prometheus.agent.param.prometheus.ali;

import lombok.Data;

import java.util.List;

/**
 * @author zhangxiaowei6
 * @Date 2024/1/15 11:05
 */

@Data
public class AliNotifyObjects {
    private String notifyObjectType;
    private Long notifyObjectId;
    private String notifyObjectName;
    private List<String> notifyChannels;
}
