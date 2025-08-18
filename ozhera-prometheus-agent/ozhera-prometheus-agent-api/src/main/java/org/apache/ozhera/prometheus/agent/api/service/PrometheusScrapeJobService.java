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

package org.apache.ozhera.prometheus.agent.api.service;

import org.apache.ozhera.prometheus.agent.param.scrapeConfig.ScrapeConfigParam;
import org.apache.ozhera.prometheus.agent.result.Result;

/**
 * @author zhangxiaowei6
 */

// Provide Prometheus with the Dubbo API for job scraping.
public interface PrometheusScrapeJobService {
    Result CreateScrapeConfig(ScrapeConfigParam param);

    Result DeleteScrapeConfig(String id);

    Result UpdateScrapeConfig(String id, ScrapeConfigParam entity);

    Result GetScrapeConfig(String id);

    Result GetScrapeConfigByName(String name);

    Result GetScrapeConfigList(Integer page_size, Integer page_no);
}