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
package org.apache.ozhera.trace.etl.manager.dubbo;

import org.apache.dubbo.config.annotation.Service;
import org.apache.ozhera.trace.etl.api.service.DataSourceService;
import org.apache.ozhera.trace.etl.api.service.TraceQueryService;
import org.apache.ozhera.trace.etl.domain.tracequery.Trace;
import org.apache.ozhera.trace.etl.domain.tracequery.TraceIdQueryVo;
import org.apache.ozhera.trace.etl.domain.tracequery.TraceListQueryVo;
import org.apache.ozhera.trace.etl.domain.tracequery.TraceQueryResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service(interfaceClass = TraceQueryService.class, group = "${dubbo.group}", version = "1.0")
public class TraceQueryServiceImpl implements TraceQueryService {

    @Autowired
    private DataSourceService dataSourceService;

    @Override
    public List<Trace> getList(TraceListQueryVo vo) {
        TraceQueryResult<List<Trace>> result = dataSourceService.getList(vo);
        if (result != null) {
            return result.getData();
        } else {
            return null;
        }
    }

    @Override
    public Trace getByTraceId(TraceIdQueryVo vo) {
        TraceQueryResult<List<Trace>> result = dataSourceService.getByTraceId(vo);
        if (result != null) {
            return result.getData().getFirst();
        } else {
            return null;
        }
    }
}
