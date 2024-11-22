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

package org.apache.ozhera.monitor.service.scrapeJob;

import com.google.gson.JsonObject;
import org.apache.ozhera.monitor.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class ScrapeJobAdapt {
    private ApplicationContext applicationContext;

    @Value("${scrape.job:staging}")
    private String ScrapeJobEnv;

    @Autowired
    private ScrapeJob scrapeJob;

    public Result addScrapeJob(JsonObject param, String identifyId, String user) {
        return scrapeJob.addScrapeJob(param,identifyId, user);
    }

    public Result editScrapeJob(Integer jobId,JsonObject param,String identifyId, String user) {
        return scrapeJob.editScrapeJob(jobId, param, identifyId, user);
    }

    public Result delScrapeJob(Integer jobId,String identifyId, String user) {
        return scrapeJob.delScrapeJob(jobId,identifyId, user);
    }

    public Result queryScrapeJob(Integer jobId, String identifyId, String user) {
        return scrapeJob.queryScrapeJob(jobId,identifyId, user);
    }

    public Result queryScrapeJobByName(String name, String identifyId, String user) {
        return scrapeJob.queryScrapeJobByName(name,identifyId, user);
    }

}
