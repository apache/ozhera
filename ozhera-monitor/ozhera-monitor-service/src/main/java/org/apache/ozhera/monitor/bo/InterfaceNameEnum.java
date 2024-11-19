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
 * zgf1
 */
public enum InterfaceNameEnum {

    STRATEGY_ENABLE(ModuleNameEnum.STRATEGY, "STRATEGY_ENABLE","策略启停"),
    STRATEGY_DELETE(ModuleNameEnum.STRATEGY, "STRATEGY_DELETE","策略删除"),
    STRATEGY_ADD(ModuleNameEnum.STRATEGY, "STRATEGY_ADD","策略添加"),
    STRATEGY_EDIT(ModuleNameEnum.STRATEGY, "STRATEGY_EDIT","策略编辑"),
    RULE_EDIT(ModuleNameEnum.RULE, "RULE_EDIT","规则编辑"),
    RULE_DELETE(ModuleNameEnum.RULE, "RULE_DELETE","规则删除"),
    ALERT_GROUP_EDIT(ModuleNameEnum.ALERT_GROUP, "ALERT_GROUP_EDIT","通知组编辑"),
    ALERT_GROUP_ADD(ModuleNameEnum.ALERT_GROUP, "ALERT_GROUP_ADD","通知组添加"),
    ALERT_GROUP_DELETE(ModuleNameEnum.ALERT_GROUP, "ALERT_GROUP_DELETE","通知组删除"),
    ;

    private ModuleNameEnum moduleName;
    private String code;
    private String message;

    InterfaceNameEnum(ModuleNameEnum moduleName, String code, String message){
        this.moduleName = moduleName;
        this.code = code;
        this.message = message;
    }

    public ModuleNameEnum getModuleName() {
        return moduleName;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
