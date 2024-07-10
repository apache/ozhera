package com.xiaomi.mone.log.manager.model.bo;

import lombok.Data;

import java.util.List;

/**
 *
 * @description
 * @version 1.0
 * @author wtt
 * @date 2024/7/10 10:54
 *
 */
@Data
public class AutoAccessLogParam {

    private Long appId;
    private String appName;
    private Long envId;
    private String envName;
    private List<String> ips;
    private String logPath;

    private String tailName;

    private Long spaceId;
    private Long storeId;
}
