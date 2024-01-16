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
package com.xiaomi.youpin.prometheus.agent.service.impl;

import com.xiaomi.youpin.prometheus.agent.Commons;
import com.xiaomi.youpin.prometheus.agent.api.service.PrometheusScrapeJobService;
import com.xiaomi.youpin.prometheus.agent.enums.ErrorCode;
import com.xiaomi.youpin.prometheus.agent.param.scrapeConfig.ScrapeConfigParam;
import com.xiaomi.youpin.prometheus.agent.result.Result;
import com.xiaomi.youpin.prometheus.agent.service.prometheus.ScrapeJobService;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
@Service(timeout = 5000, group = "${dubbo.group}")
public class PrometheusScrapeJobImpl implements PrometheusScrapeJobService {
    @Autowired
    ScrapeJobService scrapeJobService;

    @Override
    public Result CreateScrapeConfig(ScrapeConfigParam param) {
        return scrapeJobService.CreateScrapeConfig(param);
    }

    @Override
    public Result DeleteScrapeConfig(String id) {
        return scrapeJobService.DeleteScrapeConfig(id);
    }

    @Override
    public Result UpdateScrapeConfig(String id, ScrapeConfigParam entity) {
        Result result = scrapeJobService.UpdateScrapeConfig(id,entity);
        return result;
    }

    @Override
    public Result GetScrapeConfig(String id) {
        return scrapeJobService.GetScrapeConfig(id);
    }

    @Override
    public Result GetScrapeConfigByName(String name) {
        return scrapeJobService.GetScrapeConfigByName(name);
    }

    @Override
    public Result GetScrapeConfigList(Integer page_size,Integer page_no) {
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
