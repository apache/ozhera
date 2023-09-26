package com.xiaomi.hera.trace.etl.api.service;

import com.xiaomi.hera.tspandata.TSpanData;

/**
 * @author goodjava@qq.com
 * @date 2023/9/19 17:14
 */
public interface IMetricsParseService {


    void parse(TSpanData tSpanData);


}
