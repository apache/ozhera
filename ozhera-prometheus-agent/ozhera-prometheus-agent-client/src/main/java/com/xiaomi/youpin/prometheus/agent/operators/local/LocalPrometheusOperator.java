package com.xiaomi.youpin.prometheus.agent.operators.local;

import com.xiaomi.youpin.prometheus.agent.operators.BasicOperator;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/26 17:28
 */
@Slf4j
public class LocalPrometheusOperator implements BasicOperator {
    @Override
    public String printTriplicities() {
        return "local";
    }
}
