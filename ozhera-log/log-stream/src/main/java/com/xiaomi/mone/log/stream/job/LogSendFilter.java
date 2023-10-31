package com.xiaomi.mone.log.stream.job;

import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/27 10:51
 */
public interface LogSendFilter {
    boolean sendMessageSwitch(Map<String, Object> dataMap);
}
