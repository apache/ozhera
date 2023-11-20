package com.xiaomi.mone.log.parse;

import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/17 11:17
 */
public interface FieldInterceptor {

    void postProcess(Map<String, Object> data);
}
