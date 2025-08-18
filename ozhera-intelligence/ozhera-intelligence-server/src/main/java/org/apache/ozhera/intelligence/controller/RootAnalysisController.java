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
package org.apache.ozhera.intelligence.controller;

import com.xiaomi.youpin.infra.rpc.Result;
import com.xiaomi.youpin.infra.rpc.errors.GeneralCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.intelligence.domain.rootanalysis.HeraRootCaseAnalyseRes;
import org.apache.ozhera.intelligence.domain.rootanalysis.LogParam;
import org.apache.ozhera.intelligence.domain.rootanalysis.MetricsQueryParam;
import org.apache.ozhera.intelligence.service.LogService;
import org.apache.ozhera.intelligence.service.MetricsService;
import org.apache.ozhera.intelligence.service.TraceService;
import org.apache.ozhera.trace.etl.domain.tracequery.Span;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.ozhera.intelligence.domain.rootanalysis.TraceQueryParam;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analysis")
@Slf4j
public class RootAnalysisController {

    @Autowired
    private TraceService traceService;

    @Autowired
    private LogService logService;

    @Autowired
    private MetricsService metricsService;

    @PostMapping("/trace/sectional/span")
    public Result<List<Span>> traceRootAnalysis(TraceQueryParam param) {
        try {
            List<Span> spans = traceService.queryTraceRootAnalysis(param);
            return Result.success(spans);
        }catch (Exception e){
            log.error("trace analyze error , ", e);
            return Result.fail(GeneralCodes.InternalError, "trace analyze error");
        }
    }

    @PostMapping("/log/condition")
    public Result<List<Map<String, Object>>> logCondition(LogParam param) {
        try {
            List<Map<String, Object>> logs = logService.queryLogRootAnalysis(param);
            return Result.success(logs);
        }catch (Exception e){
            log.error("log analyze error , ", e);
            return Result.fail(GeneralCodes.InternalError, "log analyze error");
        }
    }

    @PostMapping("/metrics")
    public Result<HeraRootCaseAnalyseRes> metrics(MetricsQueryParam param) {
        try {
            HeraRootCaseAnalyseRes metrics = metricsService.queryMetricsRootAnalysis(param);
            return Result.success(metrics);
        }catch (Exception e){
            log.error("metrics analyze error , ", e);
            return Result.fail(GeneralCodes.InternalError, "metrics analyze error");
        }
    }
}
