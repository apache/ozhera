package com.xiaomi.mone.app.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PodInfo {
    private String name;
    private String ip;
    private String node;
    private String env;
    private String envId;

}
