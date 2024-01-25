/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package run.mone.trace.etl.extension.es;

import com.xiaomi.hera.trace.etl.api.service.DataSourceService;
import com.xiaomi.hera.trace.etl.domain.DriverDomain;
import com.xiaomi.hera.trace.etl.domain.ErrorTraceMessage;
import com.xiaomi.hera.trace.etl.domain.tracequery.Trace;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceIdQueryVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceListQueryVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceOperationsVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceQueryResult;
import com.xiaomi.hera.tspandata.TSpanData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class EsDataSourceService implements DataSourceService {

    @Autowired
    private QueryEsService queryEsService;
    @Autowired
    private WriteEsService writeEsService;

    @Override
    public TraceQueryResult<List<String>> getOperations(TraceOperationsVo vo) {
        return queryEsService.getOperations(vo.getService(), vo.getIndex());
    }

    @Override
    public TraceQueryResult<List<Trace>> getList(TraceListQueryVo vo) {
        return queryEsService.getList(vo);
    }

    @Override
    public TraceQueryResult<List<Trace>> getByTraceId(TraceIdQueryVo vo) {
        return queryEsService.getByTraceId(vo);
    }

    @Override
    public void insertErrorTrace(ErrorTraceMessage errorTraceMessage) {
        writeEsService.submitErrorEsTrace(errorTraceMessage);
    }

    @Override
    public void insertHeraTraceService(String date, String serviceName, String oprationName) {
        writeEsService.insertJaegerService(date, serviceName, oprationName);
    }

    @Override
    public void insertDriver(DriverDomain driverDomain) {
        writeEsService.insertDriver(driverDomain);
    }

    @Override
    public void insertHeraSpan(TSpanData tSpanData, String serviceName, String spanName) {
        writeEsService.insertJaegerSpan(tSpanData, serviceName, spanName);
    }
}
