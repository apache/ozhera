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
package org.apache.ozhera.log.agent.common;

import com.google.gson.*;
import org.apache.ozhera.log.agent.input.AppLogInput;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shanwb
 * @date 2021-08-02
 */
@Slf4j
public class AbstractElementAdapter implements
        JsonSerializer<Object>, JsonDeserializer<Object> {

    private static final String KEY_TYPE = "type";

    private Map<String, String> typeToClassMap;

    public AbstractElementAdapter() {
        this.typeToClassMap = loadTypeToClassMappingFromConfig();
    }


    @SneakyThrows
    @Override
    public Object deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        JsonObject jsonObj = jsonElement.getAsJsonObject();
        String implType = jsonObj.get(KEY_TYPE).getAsString();

        // Get the corresponding class object
        Class<?> clz = getClassForImplType(implType);

        return jsonDeserializationContext.deserialize(jsonElement, clz);
    }

    private Class<?> getClassForImplType(String implType) throws ClassNotFoundException {

        // Get the corresponding class name based on implType
        String className = this.typeToClassMap.get(implType);

        if (className != null && !className.isEmpty()) {
            // Loading classes using reflection
            return Class.forName(className);
        } else {
            // If the corresponding class name cannot be found, you can throw an exception or provide a default value
            return AppLogInput.class;
        }
    }

    private static Map<String, String> loadTypeToClassMappingFromConfig() {
        Map<String, String> typeToClassMap = new HashMap<>();

        String configFile = "log_impl_type.json";

        try (InputStream input = AbstractElementAdapter.class.getClassLoader().getResourceAsStream(configFile)) {
            // Parse JSON files using Gson
            JsonParser parser = new JsonParser();
            JsonObject json = parser.parse(new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();

            // Parse JSON and store the mapping in typeToClassMap
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                typeToClassMap.put(entry.getKey(), entry.getValue().getAsString());
            }
        } catch (Exception e) {
            log.error("loadTypeToClassMappingFromConfig error,fileName:{}", configFile, e);
        }

        return typeToClassMap;
    }

    @Override
    public JsonElement serialize(Object object, Type type,
                                 JsonSerializationContext jsonSerializationContext) {
        JsonElement jsonEle = jsonSerializationContext.serialize(object, object.getClass());
        return jsonEle;
    }
}