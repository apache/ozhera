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
package com.xiaomi.hera.trace.etl.manager.controller;

import com.xiaomi.hera.trace.etl.api.service.DataSourceService;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceIdQueryVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceListQueryVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceOperationsVo;
import com.xiaomi.hera.trace.etl.domain.tracequery.TraceQueryResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/11/7 11:37 上午
 */
@RestController
@RequestMapping("/tracing/v1")
@Slf4j
public class TraceController {

    @Autowired
    private DataSourceService dataSourceService;

    @Value("${es.trace.index.prefix}")
    private String spanIndexPrefix;

    @Value("${es.trace.index.service.prefix}")
    private String serviceIndexPrefix;

    @GetMapping(value = "/app/operations")
    public TraceQueryResult operations(TraceOperationsVo vo) {
        vo.setIndex(serviceIndexPrefix);
        return dataSourceService.getOperations(vo);
    }

    @GetMapping(value = "/trace/list")
    public TraceQueryResult getList(TraceListQueryVo vo) {
        vo.setIndex(spanIndexPrefix);
        return dataSourceService.getList(vo);
    }

    @GetMapping(value = "/trace/{traceId}")
    public TraceQueryResult getByTraceId(@PathVariable String traceId, TraceIdQueryVo vo) {
        vo.setIndex(spanIndexPrefix);
        vo.setTraceId(traceId);
        return dataSourceService.getByTraceId(vo);
    }
}
