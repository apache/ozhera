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
package com.xiaomi.mone.app.enums;

/**
 * @author wtt
 * @version 1.0
 * @description mimonitor对应的应用类型
 * @date 2022/10/29 15:39
 */
public enum PlatFormTypeEnum {

    CHINA(0, "china", "china", PlatForm.MIONE, "开源", 0, 0),
    ;

    private Integer code;
    private String name;
    private String grafanaDir;
    private PlatForm platForm;
    private String desc;
    private Integer marketCode;
    private Integer projectTypeCode;

    PlatFormTypeEnum(Integer code, String name, String grafanaDir, PlatForm platForm,
                     String desc, Integer marketCode, Integer projectTypeCode) {
        this.code = code;
        this.name = name;
        this.grafanaDir = grafanaDir;
        this.platForm = platForm;
        this.desc = desc;
        this.marketCode = marketCode;
        this.projectTypeCode = projectTypeCode;
    }

    public Integer getCode() {
        return code;
    }

    public Integer getMarketCode() {
        return marketCode;
    }

    public String getName() {
        return name;
    }

    public String getGrafanaDir() {
        return grafanaDir;
    }

    public PlatForm getPlatForm() {
        return platForm;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getProjectTypeCode() {
        return projectTypeCode;
    }

    public static PlatFormTypeEnum getEnum(Integer code) {
        PlatFormTypeEnum[] values = PlatFormTypeEnum.values();
        for (PlatFormTypeEnum value : values) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public static String getDescByCode(Integer code) {
        PlatFormTypeEnum[] values = PlatFormTypeEnum.values();
        for (PlatFormTypeEnum value : values) {
            if (value.getCode().equals(code)) {
                return value.getDesc();
            }
        }
        return null;
    }

    public static String getGrafanaDirByCode(Integer code) {

        PlatFormTypeEnum[] values = PlatFormTypeEnum.values();
        for (PlatFormTypeEnum value : values) {
            if (value.getCode().equals(code)) {
                return value.getGrafanaDir();
            }
        }
        return null;
    }

    public static Integer getCodeByGrafanaDir(String grafanaDir) {
        PlatFormTypeEnum[] values = PlatFormTypeEnum.values();
        for (PlatFormTypeEnum value : values) {
            if (value.getGrafanaDir().equals(grafanaDir)) {
                return value.getCode();
            }
        }
        return null;
    }

    public static boolean isCodeBlondToPlatForm(Integer code, PlatForm platForm) {

        PlatFormTypeEnum[] values = PlatFormTypeEnum.values();
        for (PlatFormTypeEnum value : values) {
            if (value.getCode().equals(code) && value.getPlatForm().equals(platForm)) {
                return true;
            }
        }
        return false;
    }

    //检查是否是合法类型
    public static Integer getMarketType(Integer code) {
        PlatFormTypeEnum[] values = PlatFormTypeEnum.values();
        for (PlatFormTypeEnum value : values) {
            if (value.getCode().equals(code)) {
                //通过code 给marketCode
                return value.getMarketCode();
            }
        }
        return -1;
    }

}
