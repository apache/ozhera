package com.xiaomi.mone.log.stream.plugin.nacos;

import lombok.Data;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/27 10:24
 */
@Data
public class LogFilterConfig {

    private Long tailId;

    private boolean enableFilter;

    private List<LogFieldFilter> logFieldFilterList;

    @Data
    public static class LogFieldFilter {
        private String logField;
        private String filterKeyWord;
    }
}
