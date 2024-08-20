/*
 *  Copyright (C) 2020 Xiaomi Corporation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.xiaomi.mone.monitor.service.scrapeJob;

import com.google.gson.JsonObject;
import com.xiaomi.mone.monitor.result.Result;

public interface ScrapeJob {
    public Result addScrapeJob(JsonObject param, String identifyId, String user);

    public Result editScrapeJob(Integer jobId,JsonObject param,String identifyId, String user);

    public Result delScrapeJob(Integer jobId,String identifyId, String user);

    public Result  queryScrapeJob(Integer jobId, String identifyId, String user);
    public Result  queryScrapeJobByName(String name, String identifyId, String user);
}
