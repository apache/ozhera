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

import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.model.StatisticsQuery;
import org.apache.ozhera.log.manager.model.dto.EsStatisticsKeyWord;
import org.apache.ozhera.log.manager.model.vo.LogQuery;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface StatisticsService {
    
    
    /**
     * Hourly data volume statistics for a single tail
     *
     * @param statisticsQuery
     * @return
     * @throws IOException
     */
    Result<Map<String, Long>> queryTailStatisticsByHour(StatisticsQuery statisticsQuery) throws IOException;
    
    
    /**
     * Top 5 in the data volume of all tails in a single store on the day
     *
     * @param statisticsQuery
     * @return
     * @throws IOException
     */
    Result<Map<String, Long>> queryStoreTopTailStatisticsByDay(StatisticsQuery statisticsQuery) throws IOException;
    
    
    /**
     * Top 5 in the volume of data of all stores in a single space
     *
     * @param statisticsQuery
     * @return
     * @throws IOException
     */
    Result<Map<String, Long>> querySpaceTopStoreByDay(StatisticsQuery statisticsQuery) throws IOException;
    
    /**
     * Query and calculate the statistical percentages of log keywords in Elasticsearch.
     *
     * @param logQuery
     * @return
     */
    Result<List<EsStatisticsKeyWord>> queryEsStatisticsRation(LogQuery logQuery);
    
    
}
