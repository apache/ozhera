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
package com.xiaomi.mone.monitor.service;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.collect.Lists;
import com.xiaomi.mone.monitor.dao.HeraAppRoleDao;
import com.xiaomi.mone.monitor.dao.model.HeraAppRole;
import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.alertmanager.AlarmExprService;
import com.xiaomi.mone.monitor.service.model.PageData;
import com.xiaomi.mone.monitor.service.model.ResourceUsageMessage;
import com.xiaomi.mone.monitor.service.model.prometheus.Metric;
import com.xiaomi.mone.monitor.service.prometheus.PrometheusService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gaoxihui
 * @date 2022/5/12 5:18 PM
 */
@Service
public class ResourceUsageService {

    @Autowired
    AlarmExprService alarmExprService;

    @Autowired
    PrometheusService prometheusService;

    @Autowired
    HeraAppRoleDao heraAppRoleDao;

    @NacosValue(value = "${resource.use.rate.alarm.config:1}",autoRefreshed = true)
    private String resourceAlarm;

    public List<ResourceUsageMessage> getCpuUsageData(){
        String mimonitor = alarmExprService.getContainerCpuResourceAlarmExpr(null, "mimonitor", "<", Integer.valueOf(resourceAlarm),false,null);
        Result<PageData> pageDataResult = prometheusService.queryByMetric(mimonitor);
        PageData data = pageDataResult.getData();
        List<ResourceUsageMessage> listMsg = new ArrayList<>();
        if(data != null){
            List<Metric> list = (List<Metric>)data.getList();
            if(CollectionUtils.isNotEmpty(list)){
                listMsg = list.stream().map(t -> {
                    List<HeraAppRole> query = heraAppRoleDao.queryByPlatTypes(t.getContainer_label_PROJECT_ID(), Lists.newArrayList(0,2),0, 5);
                    List<String> members = CollectionUtils.isEmpty(query) ? Lists.newArrayList() : query.stream().map( r -> r.getUser()).collect(Collectors.toList());
                    return new ResourceUsageMessage(t.getIp(), t.getContainer_label_PROJECT_ID(), t.getName().substring(0, t.getName().lastIndexOf("-")), String.valueOf(t.getValue()), null,members,resourceAlarm + "%");
                }).collect(Collectors.toList());
            }
        }

        return listMsg;
    }

    public List<ResourceUsageMessage> getMemUsageData(){
        String mimonitor = alarmExprService.getContainerMemReourceAlarmExpr(null, "mimonitor", "<", Integer.valueOf(resourceAlarm),false,null);
        Result<PageData> pageDataResult = prometheusService.queryByMetric(mimonitor);
        PageData data = pageDataResult.getData();
        List<ResourceUsageMessage> listMsg = new ArrayList<>();
        if(data != null){
            List<Metric> list = (List<Metric>)data.getList();
            if(CollectionUtils.isNotEmpty(list)){
                listMsg = list.stream().map(t -> {
                    List<HeraAppRole> query = heraAppRoleDao.queryByPlatTypes(t.getContainer_label_PROJECT_ID(), Lists.newArrayList(0,2),0, 5);
                    List<String> members = CollectionUtils.isEmpty(query) ? Lists.newArrayList() : query.stream().map( r -> r.getUser()).collect(Collectors.toList());
                    return new ResourceUsageMessage(t.getIp(), t.getContainer_label_PROJECT_ID(), t.getName().substring(0, t.getName().lastIndexOf("-")), String.valueOf(t.getValue()), null,members,resourceAlarm + "%");
                }).collect(Collectors.toList());
            }
        }

        return listMsg;
    }
}
