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
package org.apache.ozhera.log.manager.service;

import org.apache.ozhera.log.api.model.dto.TraceLogDTO;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.model.dto.EsStatisticResult;
import org.apache.ozhera.log.manager.model.dto.LogDTO;
import org.apache.ozhera.log.manager.model.vo.LogContextQuery;
import org.apache.ozhera.log.manager.model.vo.LogQuery;
import org.apache.ozhera.log.manager.model.vo.RegionTraceLogQuery;

import java.io.IOException;

public interface LogQueryService {

    /**
     * Read the data in the ES index
     *
     * @param logQuery
     * @return
     */
    Result<LogDTO> logQuery(LogQuery logQuery) throws Exception;

    Result<EsStatisticResult> EsStatistic(LogQuery param) throws Exception;

    /**
     * Obtain trace logs in the data center
     *
     * @param regionTraceLogQuery
     * @return
     */
    Result<TraceLogDTO> queryRegionTraceLog(RegionTraceLogQuery regionTraceLogQuery) throws IOException;

    /**
     * Obtain trace logs in the data center...
     *
     * @param logContextQuery
     * @return
     */
    Result<LogDTO> getDocContext(LogContextQuery logContextQuery);

    /**
     * Log export
     *
     * @param logQuery
     * @throws Exception
     */
    void logExport(LogQuery logQuery) throws Exception;
}
