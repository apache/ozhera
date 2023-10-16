package com.xiaomi.youpin.prometheus.agent.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author zhangxiaowei6
 * @Date 2023/10/10 16:22
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SilenceAlertManagerReqBuilder {
    private String alertName;
    private String application;
    private String userId;
    private String expectedSilenceTime;
    private String outTrackId;
    private String content;
    private String callbackTitle;
}
