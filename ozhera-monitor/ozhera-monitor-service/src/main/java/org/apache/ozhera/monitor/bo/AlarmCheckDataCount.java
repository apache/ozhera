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

import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author gaoxihui
 */
public enum AlarmCheckDataCount {

    zero("0","立即触发","Trigger immediately"),
    one("1","持续20s", "Lasts 20 seconds"),
    two("2","持续40s", "Lasts 40 seconds"),
    three("3","持续60s", "Lasts 60 seconds"),
    five("5","持续100s", "Lasts 100 seconds"),
    six("6","持续120s", "Lasts 120 seconds"),
    seven("7","持续140s", "Lasts 140 seconds"),
    eight("8","持续160s", "Lasts 160 seconds"),
    nine("9","持续180s", "Lasts 180 seconds"),
    fifteen("15","持续5m","Lasts 5 minutes");

    private String code;
    private String label;
    private String enLabel;

    AlarmCheckDataCount(String code, String label, String enLabel){
        this.code = code;
        this.label = label;
        this.enLabel = enLabel;
    }

    public static final AlarmCheckDataCount getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (AlarmCheckDataCount count : AlarmCheckDataCount.values()) {
            if (count.code.equals(code)) {
                return count;
            }
        }
        return null;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEnLabel() {
        return enLabel;
    }

    public void setEnLabel(String enLabel) {
        this.enLabel = enLabel;
    }

    public static Map<String,String> getEnumMap(){
        Map<String,String> map = new TreeMap<>();
        AlarmCheckDataCount[] values = AlarmCheckDataCount.values();
        for(AlarmCheckDataCount value : values){
            map.put(value.getCode(),value.getLabel());
        }
        return map;
    }

    public static List<Triple> getEnumList(){
        List <Triple> list = new ArrayList<>();
        AlarmCheckDataCount[] values = AlarmCheckDataCount.values();
        for(AlarmCheckDataCount value : values){
            Triple triple = new Triple(Integer.parseInt(value.getCode()),value.getLabel(), value.getEnLabel());
            list.add(triple);
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println(getEnumList());
    }

}
