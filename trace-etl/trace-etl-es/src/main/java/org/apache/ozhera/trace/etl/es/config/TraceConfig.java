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

package org.apache.ozhera.trace.etl.es.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.apache.ozhera.trace.etl.domain.HeraTraceConfigVo;
import org.apache.ozhera.trace.etl.domain.HeraTraceEtlConfig;
import org.apache.ozhera.trace.etl.service.ManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description trace config
 * @Author dingtao
 * @Date 2022/4/25 3:12 pm
 */
@Configuration
@Slf4j
public class TraceConfig {

    private ConcurrentHashMap<String, HeraTraceEtlConfig> heraTraceConfig = new ConcurrentHashMap<>();

    @NacosValue(value = "${trace.threshold}", autoRefreshed = true)
    private int threshold;
    @NacosValue(value = "${trace.duration.threshold}", autoRefreshed = true)
    private int durationThreshold;

    @Autowired
    private ManagerService managerService;

    @PostConstruct
    public void init() {
        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(() -> {
            try {
                List<HeraTraceEtlConfig> all = managerService.getAll(new HeraTraceConfigVo());
                for (HeraTraceEtlConfig config : all) {
                    heraTraceConfig.put(getServiceName(config), defaultConfig(config));
                }
            }catch(Throwable t){
                log.error("schedule trace config error : ",t);
            }
        },  0,1, TimeUnit.HOURS);
    }

    public HeraTraceEtlConfig getConfig(String serviceName) {
        return heraTraceConfig.get(serviceName);
    }

    public void insert(HeraTraceEtlConfig config) {
        log.info("trace insert config : "+config);
        heraTraceConfig.putIfAbsent(getServiceName(config), defaultConfig(config));
    }

    public void update(HeraTraceEtlConfig config) {
        log.info("trace update config : "+config);
        heraTraceConfig.put(getServiceName(config), defaultConfig(config));
    }

    public void delete(HeraTraceEtlConfig config) {
        log.info("trace delete config : "+config);
        heraTraceConfig.remove(getServiceName(config));
    }

    private String getServiceName(HeraTraceEtlConfig config) {
        StringBuffer sb = new StringBuffer();
        sb.append(config.getBindId()).append("-").append(config.getAppName());
        return sb.toString();
    }

    private HeraTraceEtlConfig defaultConfig(HeraTraceEtlConfig config){
        if(config == null){
            return null;
        }
        if(config.getTraceFilter() == null){
            config.setTraceFilter(threshold);
        }
        if(config.getTraceDurationThreshold() == null){
            config.setTraceDurationThreshold(durationThreshold);
        }
        return config;
    }
}