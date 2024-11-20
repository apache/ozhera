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

package org.apache.ozhera.monitor.service.aop.context;

import java.util.HashMap;
import java.util.Map;

/**
 * @project: mimonitor
 * @author: zgf1
 * @date: 2022/1/14 15:02
 */
public class HeraRequestMappingContext {

    private static final ThreadLocal<Map<String, Object>> local = new ThreadLocal<>();

    public static void putAll(Map<String, Object> map) {
        local.set(map);
    }

    public static Map<String, Object> getAll() {
        return local.get();
    }

    public static void clearAll() {
        local.remove();
    }

    public static void set(String key, Object value) {
       Map<String,Object> map = local.get();
       if (map == null) {
           map = new HashMap<>();
           local.set(map);
       }
       map.put(key, value);
    }

    public static <T> T get(String key) {
        Map<String,Object> map = local.get();
        if (map == null) {
            return null;
        }
        return (T)map.get(key);
    }

    public static void clear(String key) {
        Map<String, Object> map = local.get();
        if (map == null) {
            return;
        }
        map.remove(key);
    }
}
