package com.xiaomi.youpin.prometheus.agent.param.prometheus.ali;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * @author zhangxiaowei6
 * @Date 2024/1/12 16:39
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Scrape_configs {

   private List<com.xiaomi.youpin.prometheus.agent.param.prometheus.Scrape_configs> scrape_configs;

}
