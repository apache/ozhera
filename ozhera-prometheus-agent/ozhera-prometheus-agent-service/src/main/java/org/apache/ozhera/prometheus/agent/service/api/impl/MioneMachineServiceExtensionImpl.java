/*
 * Copyright (C) 2020 Xiaomi Corporation
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
package org.apache.ozhera.prometheus.agent.service.api.impl;

import org.apache.ozhera.prometheus.agent.domain.Ips;
import org.apache.ozhera.prometheus.agent.service.api.MioneMachineServiceExtension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * @author zhangxiaowei6
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class MioneMachineServiceExtensionImpl implements MioneMachineServiceExtension {
    @Override
    public List<Ips> queryMachineList(String type) {
        return null;
    }
}
