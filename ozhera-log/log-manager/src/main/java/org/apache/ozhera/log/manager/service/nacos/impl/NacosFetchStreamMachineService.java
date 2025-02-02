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
package org.apache.ozhera.log.manager.service.nacos.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.collect.Lists;
import com.xiaomi.data.push.nacos.NacosNaming;
import org.apache.ozhera.log.manager.common.exception.MilogManageException;
import org.apache.ozhera.log.manager.service.extension.common.CommonExtensionServiceFactory;
import org.apache.ozhera.log.manager.service.nacos.FetchStreamMachineService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.STREAM_CONTAINER_POD_NAME_KEY;
import static org.apache.ozhera.log.common.Constant.STRIKETHROUGH_SYMBOL;

@Slf4j
public class NacosFetchStreamMachineService implements FetchStreamMachineService {

    private final NacosNaming nacosNaming;

    public NacosFetchStreamMachineService(NacosNaming nacosNaming) {
        this.nacosNaming = nacosNaming;
    }

    @Override
    public List<String> streamMachineUnique() {
        preCheckNaming(nacosNaming);
        List<String> uniqueKeys = Lists.newArrayList();
        try {
            List<Instance> allInstances = nacosNaming.getAllInstances(CommonExtensionServiceFactory.getCommonExtensionService().getHeraLogStreamServerName());
            return extractUniqueKeys(allInstances);
        } catch (NacosException e) {
            log.error("nacos queryStreamMachineIps error", e);
        }
        return uniqueKeys;
    }

    @Override
    public List<String> getStreamList(String dataId) {
        preCheckNaming(nacosNaming);
        List<String> uniqueKeys = Lists.newArrayList();
        try {
            List<Instance> allInstances = nacosNaming.getAllInstances(dataId);
            return extractUniqueKeys(allInstances);
        } catch (NacosException e) {
            log.error("nacos queryStreamMachineIps error,dataId:{}", dataId, e);
        }
        return uniqueKeys;
    }

    private void preCheckNaming(NacosNaming nacosNaming) {
        if (null == nacosNaming) {
            throw new MilogManageException("please set nacos naming first");
        }
    }

    private List<String> extractUniqueKeys(List<Instance> instances) {
        return instances.stream()
                .map(this::extractKeyFromInstance)
                .distinct()
                .collect(Collectors.toList());
    }

    private String extractKeyFromInstance(Instance instance) {
        if (instance.getMetadata().containsKey(STREAM_CONTAINER_POD_NAME_KEY)) {
            return StringUtils.substringAfterLast(instance.getMetadata().get(STREAM_CONTAINER_POD_NAME_KEY), STRIKETHROUGH_SYMBOL);
        } else {
            return instance.getIp();
        }
    }

}
