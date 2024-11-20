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

/**
 * @author gaoxihui
 * @date 2021/12/2 5:27 下午
 */
public enum PlatFormType {
    open(0,"open","open", PlatForm.open,"开源组",0),
    ;

    private Integer code;
    private String name;
    private String grafanaDir;
    private PlatForm platForm;
    private String desc;
    private Integer marketCode;

    PlatFormType(Integer code, String name, String grafanaDir,PlatForm platForm,String desc,Integer marketCode) {
        this.code = code;
        this.name = name;
        this.grafanaDir = grafanaDir;
        this.platForm = platForm;
        this.desc = desc;
        this.marketCode = marketCode;
    }

    public Integer getCode() {
        return code;
    }

    public Integer getMarketCode(){
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

}
