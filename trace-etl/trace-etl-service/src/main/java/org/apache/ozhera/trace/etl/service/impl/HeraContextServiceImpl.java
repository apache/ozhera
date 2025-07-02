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

package org.apache.ozhera.trace.etl.service.impl;

import org.apache.ozhera.trace.etl.service.HeraContextService;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * @Description
 * @Date 2022/7/6 7:49 pm
 */
@Service
public class HeraContextServiceImpl implements HeraContextService {
    
    @Override
    public Set<String> getHeraContextKeys(String heraContext) {
        Set<String> result = new HashSet<>();
        String[] split = heraContext.split(";");
        for (String keyValue : split) {
            String[] kv = keyValue.split(":");
            result.add(kv[0]);
        }
        return result;
    }
    
    @Override
    public String getHeraContextValue(String heraContext, String key) {
        String[] split = heraContext.split(";");
        for (String keyValue : split) {
            String[] kv = keyValue.split(":");
            if (key.equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }
}