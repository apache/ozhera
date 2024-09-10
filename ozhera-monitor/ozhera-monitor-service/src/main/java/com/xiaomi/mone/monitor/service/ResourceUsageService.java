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
public interface ResourceUsageService {


    public List<ResourceUsageMessage> getCpuUsageData();

    public List<ResourceUsageMessage> getMemUsageData();
}
