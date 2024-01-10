package com.xiaomi.hera.trace.etl.api.service;

import com.xiaomi.hera.trace.etl.domain.DriverDomain;
import com.xiaomi.hera.trace.etl.domain.ErrorTraceMessage;
import com.xiaomi.hera.trace.etl.domain.tracequery.Trace;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceIdQueryVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceListQueryVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceOperationsVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceQueryResult;
import com.xiaomi.hera.tspandata.TSpanData;

import java.util.List;

public interface DataSourceService {

    TraceQueryResult<List<String>> getOperations(TraceOperationsVo vo);

    TraceQueryResult<List<Trace>> getList(TraceListQueryVo vo);

    TraceQueryResult<List<Trace>> getByTraceId(TraceIdQueryVo vo);

    void insertErrorTrace(ErrorTraceMessage errorTraceMessage);

    void insertHeraTraceService(String date, String serviceName, String operationName);

    void insertDriver(DriverDomain driverDomain);

    void insertHeraSpan(TSpanData tSpanData, String serviceName, String spanName);
}
