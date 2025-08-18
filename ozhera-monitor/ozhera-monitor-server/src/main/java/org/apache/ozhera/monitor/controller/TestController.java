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

import org.apache.ozhera.monitor.dao.AppGrafanaMappingDao;
import org.apache.ozhera.monitor.dao.model.AppGrafanaMapping;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.AA;
import org.apache.ozhera.monitor.service.AppMonitorService;
import org.apache.ozhera.monitor.service.GrafanaService;
import org.apache.ozhera.monitor.service.model.AppMonitorRequest;
import org.apache.ozhera.monitor.service.model.PageData;
import org.apache.ozhera.monitor.service.prometheus.MetricSuffix;
import org.apache.ozhera.monitor.service.prometheus.PrometheusService;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import com.xiaomi.youpin.feishu.FeiShu;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;


/**
 * @author gaoxihui
 * @date 2021/7/6 2:05 PM
 */
@Slf4j
@RestController
public class TestController {
    private FeiShu feiShu;

    @Reference(registry = "registryConfig",check = false, interfaceClass = AA.class, group = "${dubbo.group}")
    private AA aa;

    @Autowired
    private GrafanaService grafanaImpl;


    @GetMapping("/test")
    public String test() {
        aa.testA();
        return "OK!";
    }


    @PostMapping("/test/alertManager")
    public void testAlertManager(@RequestBody String body) {
        System.out.println(body);
    }

    @Autowired
    AppGrafanaMappingDao appGrafanaMappingDao;

    @Autowired
    AppMonitorService appMonitorService;


    @ResponseBody
    @PostMapping("/getProjects")
    public Result<PageData> getProjectInfos(@RequestBody AppMonitorRequest param){
        return appMonitorService.getProjectInfos(null,param.getAppName(),param.getPage(),param.getPageSize());
    }

    @GetMapping("/testDb")
    public String testDb(){
        AppGrafanaMapping app = new AppGrafanaMapping();
        app.setAppName("app2");
        app.setGrafanaUrl("http://baidu.com");
        int i = appGrafanaMappingDao.generateGrafanaMapping(app);
        System.out.println("result= " + i);

        AppGrafanaMapping app2 = appGrafanaMappingDao.getByAppName("app2");
        return app2.toString();
    }

    @Autowired
    PrometheusService prometheusService;

    /**
     * Interval vector query example
     * @return
     */
    @ResponseBody
    @GetMapping("/testpT")
    public Result<PageData> testPrometheusTotal(String metricP, Long startTimeP, Long endTimeP, Long stepP){

        Long startTime = startTimeP;
        if(startTime  == null){
            startTime = (System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) / 1000L;
        }

        Long endTime = endTimeP;
        if(endTime  == null){
            endTime = System.currentTimeMillis() / 1000L;
        }


        Long step = stepP;
        if(step == null){
            step = 24 * 60 * 60L;
        }

        String metric = metricP;
        if(StringUtils.isEmpty(metric)){
            metric = "sqlSuccessCount";
        }

        return prometheusService.queryRange(metric,null,"667_zzytest", MetricSuffix._total.name(),startTime,endTime,step,null,0);
    }

    @ResponseBody
    @GetMapping("/testpC")
    public Result<PageData> testPrometheusCount(String metricP, String op, double value, String timeUnit, Long timeDuration, Long startTimeP, Long endTimeP, Long stepP){

        Long startTime = startTimeP;

        if(timeDuration != null && !StringUtils.isEmpty(timeUnit)){

        }

        if(startTime  == null){
            startTime = (System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) / 1000L;
        }

        Long endTime = endTimeP;
        if(endTime  == null){
            endTime = System.currentTimeMillis() / 1000L;
        }


        Long step = stepP;
        if(step == null){
            step = 24 * 60 * 60L;
        }

        String metric = metricP;
        if(StringUtils.isEmpty(metric)){
            metric = "sqlSuccessCount";
        }

//        Map<String,String> map = new HashMap<>();
//        map.put("dataSource","/test");

        return prometheusService.queryRange(metric,null,"667_zzytest", MetricSuffix._count.name(),startTime,endTime,step,op,value);
    }

    /**
     * Group query test
     * @return
     */
    @GetMapping("/testpg")
    public String testGroupPrometheus(HttpServletRequest request, String metricP, Long startTimeP, Long endTimeP, Long stepP){

       // UserInfoVO user = AegisFacade.getUserInfo(request);
        AuthUserVo user = UserUtil.getUser();
        log.info("cas user info : {} ", user.toString());

        Long startTime = startTimeP;
        if(startTime  == null){
            startTime = (System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000) / 1000L;
        }

        Long endTime = endTimeP;
        if(endTime  == null){
            endTime = System.currentTimeMillis() / 1000L;
        }


        Long step = stepP;
        if(step == null){
            step = 24 * 60 * 60L;
        }

        String metric = metricP;
        if(StringUtils.isEmpty(metric)){
            metric = "dubboBisTotalCount";
        }

        List groups = Arrays.asList("serverIp","serviceName","methodName");

        String s = prometheusService.queryRangeSum(metric,null,"667_zzytest",MetricSuffix._total.name(),startTime,endTime,step,groups);
        return s;

    }
}
