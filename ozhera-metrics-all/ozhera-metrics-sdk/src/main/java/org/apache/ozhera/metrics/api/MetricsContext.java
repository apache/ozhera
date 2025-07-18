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
package org.apache.ozhera.metrics.api;

import com.google.gson.Gson;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @version 1.0
 * @description
 * @date 2025/5/13 16:56
 */
public class MetricsContext {

    private Map<String, String> mapContent = new HashMap<>();
    @Getter
    private static final Gson gson = new Gson();

    public static final String CUSTOM_DELIMITER = "#$@";

    public MetricsContext() {
    }

    public void addContext(String key, String value) {
        mapContent.put(key, value);
    }

    public String getContext(String key) {
        return mapContent.get(key);
    }

    public String toJson() {
        return gson.toJson(mapContent);
    }
}
