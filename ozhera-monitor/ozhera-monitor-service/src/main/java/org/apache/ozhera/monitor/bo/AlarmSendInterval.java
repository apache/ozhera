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
public enum AlarmSendInterval {

    interval_5m("5m","5分钟报警一次", "Alarm every 5 minutes"),
    interval_15m("15m","15分钟报警一次", "Alarm every 15 minutes"),
    interval_30m("30m","30分钟报警一次", "Alarm every 30 minutes"),
    interval_1h("1h","1小时报警一次", "Alarm every 1 hour"),
    interval_2h("2h","2小时报警一次", "Alarm every 2 hours");

    private String code;
    private String label;
    private String labelEn;

    AlarmSendInterval(String code,String label, String labelEn){
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
        Map<String,String> map = new LinkedHashMap<>();
        AlarmSendInterval[] values = AlarmSendInterval.values();
        for(AlarmSendInterval value : values){
            map.put(value.getCode(),value.getLabelEn());
        }
        return map;
    }

    public static List<Triple> getEnumList(){
        List <Triple> list = new ArrayList<>();
        AlarmSendInterval[] values = AlarmSendInterval.values();
        for(AlarmSendInterval value : values){
            Triple triple = new Triple(value.getCode(), value.getLabel(), value.getLabelEn());
            list.add(triple);
        }
        return list;
    }

    public static AlarmSendInterval getEnum(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (AlarmSendInterval interval : AlarmSendInterval.values()) {
            if (interval.code.equals(code)) {
                return interval;
            }
        }
        return null;
    }


    public static void main(String[] args) {
        System.out.println(getEnumMap());
    }

}
