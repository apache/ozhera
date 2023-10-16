package com.xiaomi.youpin.prometheus.agent.service.dto.dingding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
/**
 * @author zhangxiaowei6
 * @Date 2023/10/9 20:11
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SilenceCallBack {
    private String outTrackId;
    private String corpId;
    private String userId;
    private String content;
}
