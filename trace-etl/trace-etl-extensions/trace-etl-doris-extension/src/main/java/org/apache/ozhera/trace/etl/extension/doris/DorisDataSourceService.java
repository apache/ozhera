/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ozhera.trace.etl.extension.doris;

import org.apache.ozhera.trace.etl.api.service.DataSourceService;
import org.apache.ozhera.trace.etl.domain.DriverDomain;
import org.apache.ozhera.trace.etl.domain.ErrorTraceMessage;
import org.apache.ozhera.trace.etl.domain.tracequery.Trace;
import org.apache.ozhera.trace.etl.domain.tracequery.TraceIdQueryVo;
import org.apache.ozhera.trace.etl.domain.tracequery.TraceListQueryVo;
import org.apache.ozhera.trace.etl.domain.tracequery.TraceOperationsVo;
import org.apache.ozhera.trace.etl.domain.tracequery.TraceQueryResult;
import org.apache.ozhera.tspandata.TSpanData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DorisDataSourceService implements DataSourceService {

    @Autowired
    private QueryDorisService queryDorisService;
    @Autowired
    private WriteDorisService writeDorisService;

    @Override
    public TraceQueryResult<List<String>> getOperations(TraceOperationsVo vo) {
        return queryDorisService.getOperations(vo.getService(), vo.getIndex());
    }

    @Override
    public TraceQueryResult<List<Trace>> getList(TraceListQueryVo vo) {
        return queryDorisService.getList(vo);
    }

    @Override
    public TraceQueryResult<List<Trace>> getByTraceId(TraceIdQueryVo vo) {
        return queryDorisService.getByTraceId(vo);
    }

    @Override
    public void insertErrorTrace(ErrorTraceMessage errorTraceMessage) {
        writeDorisService.insertErrorTrace(errorTraceMessage);
    }

    @Override
    public void insertHeraTraceService(String date, String serviceName, String operationName) {
        writeDorisService.insertHeraTraceService(serviceName, operationName);
    }

    @Override
    public void insertDriver(DriverDomain driverDomain) {
        writeDorisService.insertDriver(driverDomain);
    }

    @Override
    public void insertHeraSpan(TSpanData tSpanData, String serviceName, String spanName) {
        writeDorisService.insertHeraSpan(tSpanData, serviceName, spanName);
    }
}