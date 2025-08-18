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

package org.apache.ozhera.prometheus.agent.controller;

import org.apache.ozhera.prometheus.agent.Commons;
import org.apache.ozhera.prometheus.agent.enums.ErrorCode;
import org.apache.ozhera.prometheus.agent.param.scrapeConfig.ScrapeConfigParam;
import org.apache.ozhera.prometheus.agent.result.Result;
import org.apache.ozhera.prometheus.agent.service.prometheus.ScrapeJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

//Grab exporter job related interfaces.

/**
 * @author zhangxiaowei6
 */
@RestController
@Slf4j
@RequestMapping(value = "/api/v1")
public class PrometheusScrapeJobController {

    @Autowired
    ScrapeJobService scrapeJobService;

    @RequestMapping(value = "/scrape-config", method = RequestMethod.POST)
    public Result CreateScrapeConfig(@RequestBody ScrapeConfigParam param) {
        return scrapeJobService.CreateScrapeConfig(param);
    }

    @RequestMapping(value = "/scrape-config/{id}", method = RequestMethod.DELETE)
    public Result DeleteScrapeConfig(@PathVariable String id) {
        return scrapeJobService.DeleteScrapeConfig(id);
    }

    @RequestMapping(value = "/scrape-config/{id}", method = RequestMethod.PUT)
    public Result UpdateScrapeConfig(@PathVariable String id, @RequestBody ScrapeConfigParam entity) {
        Result result = scrapeJobService.UpdateScrapeConfig(id, entity);
        return result;
    }

    @RequestMapping(value = "/scrape-config/{id}", method = RequestMethod.GET)
    public Result GetScrapeConfig(@PathVariable String id) {
        return scrapeJobService.GetScrapeConfig(id);
    }

    @RequestMapping(value = "/scrape-config/list", method = RequestMethod.GET)
    public Result GetScrapeConfigList(Integer page_size, Integer page_no) {
        if (page_size == null && page_no == null) {
            return Result.fail(ErrorCode.invalidParamError);
        }
        if (page_size == null) {
            page_size = Commons.COMMON_PAGE_SIZE;
        }
        if (page_no == null) {
            page_no = Commons.COMMON_PAGE_NO;
        }

        Result result = scrapeJobService.GetScrapeConfigList(page_size, page_no);
        return result;
    }

}