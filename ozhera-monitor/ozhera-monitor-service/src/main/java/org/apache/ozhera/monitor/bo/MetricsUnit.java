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

import java.util.*;

/**
 * @author gaoxihui
 */
public enum MetricsUnit {

    UNIT_PERCENT("%","百分比"),
    UNIT_MS("ms","毫秒"),
    UNIT_S("s","秒"),
    UNIT_COUNT("次","数量"),
    GB_COUNT("GB","GB"),
    UNIT_NULL("","无单位"),
    UNIT_TAI("台","台"),
    ;
    private String code;
    private String message;

    MetricsUnit(String code, String message){
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
        MetricsUnit[] values = MetricsUnit.values();
        for(MetricsUnit value : values){
            map.put(value.getCode(),value.getMessage());
        }
        return map;
    }

    public static List<Pair> getEnumList(){
        List <Pair> list = new ArrayList<>();
        MetricsUnit[] values = MetricsUnit.values();
        for(MetricsUnit value : values){
            Pair pair = new Pair(value.getCode(),value.getMessage());
            list.add(pair);
        }
        return list;
    }

}
