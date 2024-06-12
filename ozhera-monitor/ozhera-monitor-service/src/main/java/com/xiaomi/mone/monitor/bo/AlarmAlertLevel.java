package com.xiaomi.mone.monitor.bo;

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
