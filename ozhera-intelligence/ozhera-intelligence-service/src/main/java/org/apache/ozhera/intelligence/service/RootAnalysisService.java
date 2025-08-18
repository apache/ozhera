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
package org.apache.ozhera.intelligence.service;

import com.xiaomi.youpin.infra.rpc.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.ozhera.intelligence.domain.rootanalysis.MarkDownParam;
import org.apache.ozhera.intelligence.domain.rootanalysis.TraceQueryParam;

@Service
public class RootAnalysisService {

    @Autowired
    private TraceService traceService;

    @Autowired
    private LogService logService;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private PromptService promptService;

    /**
     * Root cause analysis based on trace, utilizing a large model to analyze the fundamental causes of trace issues.
     * First, analyze based on trace data; if unsuccessful, analyze using logs.
     * If the issue still cannot be identified, then analyze using metrics.
     * Finally, aggregate the results and output them to the frontend.
     *
     * @return
     */
    public Result<String> traceRootAnalysis(TraceQueryParam param) {
        return null;
    }

    /**
     * Generate the results required by the frontend in markdown format based on the summarized content.
     * @param param
     * @return
     */
    private String generateMarkDownResult(MarkDownParam param){
        return null;
    }
}
