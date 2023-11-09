package com.xiaomi.youpin.prometheus.agent.service.dto.heraApp;

import lombok.Data;

import java.util.List;

/**
 * @author zhangxiaowei6
 * @Date 2023/11/9 15:54
 */
@Data
public class GetAllPodIpRes {
    private int code;
    private String message;
    private List<String> data;
    private boolean success;
}
