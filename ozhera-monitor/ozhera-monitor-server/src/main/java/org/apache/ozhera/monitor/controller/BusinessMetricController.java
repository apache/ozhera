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
package org.apache.ozhera.monitor.controller;

import com.xiaomi.mone.tpc.login.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.monitor.controller.model.PromQueryRangeParam;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.BusinessMetricService;
import org.apache.ozhera.monitor.service.model.PageData;
import org.apache.ozhera.monitor.service.prometheus.MetricSuffix;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 
 * @date 2025/4/23 4:38 下午
 */
@Slf4j
@Controller
public class BusinessMetricController {


    @Autowired
    private BusinessMetricService businessMetricService;


    @ResponseBody
    @PostMapping("/business/metric/queryRange")
    public Result<PageData> queryRange(@RequestBody PromQueryRangeParam param){

        log.info("PrometheusController.queryRange request param : {} ",param.toString());

        Long startTime = param.getStartTime() != null ? param.getStartTime()/1000 : (System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) / 1000L; //Default query is the last 7 days

        Long endTime = param.getEndTime()!=null ? param.getEndTime()/1000 : System.currentTimeMillis() / 1000L;

        Long step = param.getStep() != null ? param.getStep() : (endTime - startTime)/2; // The default is the query time interval, that is, step = query interval/2

        String projectName = new StringBuilder().append(param.getProjectId()).append("_").append(param.getProjectName()).toString();

        MetricSuffix metricSuffix = MetricSuffix.getByName(param.getMetricSuffix()) != null ? MetricSuffix.getByName(param.getMetricSuffix()) : MetricSuffix._count;

        log.info("PrometheusController.queryRange request afterConvert Param startTime : {} ,endTime : {} ,step : {},projectName : {},metricSuffix : {}",startTime,endTime,step,projectName,metricSuffix);

        return businessMetricService.queryRange(param.getMetric(),param.getLabels(), projectName, metricSuffix.name(),startTime,endTime,step,param.getOp(),param.getValue());

    }

    @ResponseBody
    @PostMapping("/business/metric/queryIncrease")
    public Result<PageData> queryIncrease(@RequestBody PromQueryRangeParam param){

        Long startTime = param.getStartTime() != null ? param.getStartTime()/1000 : (System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) / 1000L; //Default query is the last 7 days

        Long endTime = param.getEndTime()!=null ? param.getEndTime()/1000 : System.currentTimeMillis() / 1000L;

        Long duration = endTime - startTime;

        Long step = param.getStep() != null ? param.getStep() : duration; // The default is the query time interval, that is, step = query interval

        String projectName = new StringBuilder().append(param.getProjectId()).append("_").append(param.getProjectName()).toString();

        log.info("BusinessMetricController.queryIncrease request Param startTime : {} ,endTime : {} ,step : {},projectName : {},param : {}",startTime,endTime,step,projectName, GsonUtil.gsonString(param));

        String pDuration = duration + "s";

        Result<PageData> pageDataResult = businessMetricService.queryRangeSumOverTime(param.getMetric(), param.getLabels(),  param.getMetricSuffix(), startTime, endTime, step, pDuration,param.getSumBy());


        return pageDataResult;
    }
}
