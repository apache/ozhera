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

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.ozhera.log.api.model.dto.LogFilterOptions;
import org.apache.ozhera.log.api.service.HeraLogApiService;
import org.springframework.stereotype.Service;
import org.apache.ozhera.intelligence.domain.rootanalysis.LogParam;

import java.util.List;
import java.util.Map;

@Service
public class LogService {

    @DubboReference(interfaceClass = HeraLogApiService.class, group = "${log.query.group}", version = "${log.query.version}")
    private HeraLogApiService heraLogApiService;

    /**
     * To cover the trace time in the log, we extend the trace time by 5 minutes before and after as a condition to query the log
     */
    private static final int LOG_QUERY_TIME_GAP = 1000 * 60 * 5;


	/**
     * Query logs based on the specified log query conditions.
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> queryLogRootAnalysis(LogParam param) {
        List<Map<String, Object>> maps = heraLogApiService.queryLogData(buildLogFilterOptions(param));
        return maps;
    }


    private LogFilterOptions buildLogFilterOptions(LogParam param) {
        LogFilterOptions logFilterOptions = new LogFilterOptions();
        logFilterOptions.setProjectId(Long.parseLong(param.getApplication()));
        logFilterOptions.setEnvId(Long.parseLong(param.getEnvId()));
        logFilterOptions.setTraceId(param.getTraceId());
        Long startTime = Long.parseLong(param.getStartTime());
        Long duration = Long.parseLong(param.getDuration());
        Long endTime = startTime + duration;
        logFilterOptions.setStartTime(String.valueOf(startTime / 1000 - LOG_QUERY_TIME_GAP));
        logFilterOptions.setEndTime(String.valueOf(endTime / 1000 + LOG_QUERY_TIME_GAP));
        logFilterOptions.setLevel(param.getLevel());
        return logFilterOptions;
    }
}