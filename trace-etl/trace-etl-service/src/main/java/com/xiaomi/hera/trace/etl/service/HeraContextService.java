/*
 * Copyright 2020 Xiaomi
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
package com.xiaomi.hera.trace.etl.service;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/7/6 7:49 下午
 */
@Service
public class HeraContextService {

    public Set<String> getHeraContextKeys(String heraContext){
        Set<String> result = new HashSet<>();
        String[] split = heraContext.split(";");
        for(String keyValue : split){
            String[] kv = keyValue.split(":");
            result.add(kv[0]);
        }
        return result;
    }

    public String getHeraContextValue(String heraContext, String key){
        String[] split = heraContext.split(";");
        for(String keyValue : split){
            String[] kv = keyValue.split(":");
            if(key.equals(kv[0])){
                return kv[1];
            }
        }
        return null;
    }
}
