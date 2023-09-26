package com.xiaomi.mone.monitor.service.model.prometheus;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2021/8/26 8:51 下午
 * Instantaneous vector query data model with only one value
 */
@Data
public class MetricDataSetVector implements Serializable {
    private Metric metric;
    private List<String> value;
}
