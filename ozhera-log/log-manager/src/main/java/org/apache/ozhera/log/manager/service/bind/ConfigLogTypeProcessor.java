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
package org.apache.ozhera.log.manager.service.bind;

import org.apache.ozhera.log.common.Config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wtt
 * @version 1.0
 * @description Determine how to read the configuration file
 * @date 2022/12/23 14:02
 */
//@Processor(isDefault = true, order = 100)
public class ConfigLogTypeProcessor implements LogTypeProcessor {

    private final Config config;

    public ConfigLogTypeProcessor(Config config) {
        this.config = config;
    }

    @Override
    public boolean supportedConsume(Integer type) {
        String notConsume = config.get("log_type_mq_not_consume", "");
        List<Integer> logTypesNotConsume = Arrays.stream(notConsume.split(","))
                .map(Integer::valueOf).collect(Collectors.toList());
        return !logTypesNotConsume.contains(type);
    }
}
