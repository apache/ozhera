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
package org.apache.ozhera.monitor.bo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gaoxihui
 */
public enum SendAlertGroupKey {

    /**
     * Alarm Inhibit groupKey
     */
    APP_INSTANCE("{{$labels.instance}}","实例级别抑制告警"),
    TESLA_URl("{{$labels.url}}","url级别抑制告警"),
    APP_METHOD("{{$labels.application}}_{{$labels.methodName}}","方法级别抑制告警"),
    APP_SQL_METHOD("{{$labels.application}}_{{$labels.sqlMethod}}","sql方法级别抑制告警"),
    APP("{{$labels.application}}","应用级别"),
    ;
    private String code;
    private String message;

    SendAlertGroupKey(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static Map<String,String> getEnumMap(){
        Map<String,String> map = new LinkedHashMap<>();
        SendAlertGroupKey[] values = SendAlertGroupKey.values();
        for(SendAlertGroupKey value : values){
            map.put(value.getCode(),value.getMessage());
        }
        return map;
    }

    public static List<Pair> getEnumList(){
        List <Pair> list = new ArrayList<>();
        SendAlertGroupKey[] values = SendAlertGroupKey.values();
        for(SendAlertGroupKey value : values){
            Pair pair = new Pair(value.getCode(),value.getMessage());
            list.add(pair);
        }
        return list;
    }

}
