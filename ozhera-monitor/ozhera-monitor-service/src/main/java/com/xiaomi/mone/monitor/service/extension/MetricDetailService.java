package com.xiaomi.mone.monitor.service.extension;

import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.model.prometheus.MetricDetailQuery;

public interface MetricDetailService {
    Result metricDetail(MetricDetailQuery param);
}
