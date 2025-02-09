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

package org.apache.ozhera.monitor.service.scrapeJob.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.scrapeJob.ScrapeJob;
import org.apache.ozhera.prometheus.agent.api.service.PrometheusScrapeJobService;
import org.apache.ozhera.prometheus.agent.param.scrapeConfig.ScrapeConfigParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service(value = "openSourceScrapeJob")
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class ScrapeJobImpl implements ScrapeJob {

    @Value("${dubbo.group.alert}")
    private String alert;
    @Reference(registry = "registryConfig", check = false, interfaceClass = PrometheusScrapeJobService.class, group = "${dubbo.group.alert}")
    PrometheusScrapeJobService prometheusScrapeJobService;

    @Override
    public Result addScrapeJob(JsonObject param, String identifyId, String user) {
        Result result = null;
        try {
            ScrapeConfigParam scrapeConfigParam = new Gson().fromJson(new Gson().toJson(param), ScrapeConfigParam.class);
            org.apache.ozhera.prometheus.agent.result.Result scrapeResult = prometheusScrapeJobService.CreateScrapeConfig(scrapeConfigParam);
            log.info("addScrapeJob: {}", scrapeResult);
            result = new Gson().fromJson(new Gson().toJson(scrapeResult), Result.class);

            log.info("open scrape job add, request : {} ,result:{}", new Gson().toJson(scrapeConfigParam), new Gson().toJson(scrapeResult));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.success(result.getData());
    }

    @Override
    public Result editScrapeJob(Integer jobId, JsonObject param, String identifyId, String user) {
        Result result = null;
        try {
            ScrapeConfigParam scrapeConfigParam = new Gson().fromJson(new Gson().toJson(param), ScrapeConfigParam.class);
            org.apache.ozhera.prometheus.agent.result.Result scrapeResult = prometheusScrapeJobService.UpdateScrapeConfig(String.valueOf(jobId), scrapeConfigParam);
            log.info("editScrapeJob: {}", scrapeResult);
            result = new Gson().fromJson(new Gson().toJson(scrapeResult), Result.class);
            log.info("open scrape job edit, request : {} ,result:{}", new Gson().toJson(scrapeConfigParam), new Gson().toJson(scrapeResult));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.success(result.getData());
    }

    @Override
    public Result delScrapeJob(Integer jobId, String identifyId, String user) {
        Result result = null;
        try {
            org.apache.ozhera.prometheus.agent.result.Result scrapeResult = prometheusScrapeJobService.DeleteScrapeConfig(String.valueOf(jobId));
            log.info("delScrapeJob: {}", scrapeResult);
            result = new Gson().fromJson(new Gson().toJson(scrapeResult), Result.class);
            log.info("open scrape job delete, request : {} ,result:{}", jobId, new Gson().toJson(scrapeResult));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.success(result.getData());
    }

    @Override
    public Result queryScrapeJob(Integer jobId, String identifyId, String user) {
        Result result = null;
        try {
            org.apache.ozhera.prometheus.agent.result.Result scrapeResult = prometheusScrapeJobService.GetScrapeConfig(String.valueOf(jobId));
            log.info("queryScrapeJob: {}", scrapeResult);
            result = new Gson().fromJson(new Gson().toJson(scrapeResult), Result.class);
            log.info("open scrape job query, request : {} ,result:{}", jobId, new Gson().toJson(scrapeResult));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.success(result.getData());
    }

    @Override
    public Result queryScrapeJobByName(String name, String identifyId, String user) {
        Result result = null;
        try {
            org.apache.ozhera.prometheus.agent.result.Result scrapeResult = prometheusScrapeJobService.GetScrapeConfigByName(name);
            log.info("queryScrapeJobByName: {}", scrapeResult);
            result = new Gson().fromJson(new Gson().toJson(scrapeResult), Result.class);
            log.info("open scrape job query, request : {} ,result:{}", name, new Gson().toJson(scrapeResult));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return Result.success(result.getData());
    }
}
