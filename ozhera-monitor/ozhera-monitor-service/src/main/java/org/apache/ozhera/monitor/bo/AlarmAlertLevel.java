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
public enum AlarmAlertLevel {

    P0("P0","P0","P0"),
    P1("P1","P1","P1"),
    P2("P2","P2","P2");

    private String code;
    private String label;
    private String labelEn;

    AlarmAlertLevel(String code, String label, String labelEn){
        this.code = code;
        this.label = label;
        this.labelEn = labelEn;
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

    public String getLabelEn() {
        return labelEn;
    }

    public void setLabelEn(String labelEn) {
        this.labelEn = labelEn;
    }

    public static Map<String,String> getEnumMap(){
        Map<String,String> map = new TreeMap<>();
        AlarmAlertLevel[] values = AlarmAlertLevel.values();
        for(AlarmAlertLevel value : values){
            map.put(value.getCode(),value.getLabel());
        }
        return map;
    }

    public static List<Triple> getEnumList(){
        List <Triple> list = new ArrayList<>();
        AlarmAlertLevel[] values = AlarmAlertLevel.values();
        for(AlarmAlertLevel value : values){
            Triple triple = new Triple(value.getCode(), value.getLabel(), value.getLabelEn());
            list.add(triple);
        }
        return list;
    }

    public static void main(String[] args) {
        System.out.println(getEnumMap());
    }

}
