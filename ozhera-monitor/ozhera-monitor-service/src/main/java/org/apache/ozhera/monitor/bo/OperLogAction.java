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
 * @project: mimonitor
 * @author: zgf1
 * @date: 2022/1/14 14:31
 */
public enum OperLogAction {
    STRATEGY_START("STRATEGY_START", "策略启动"),
    STRATEGY_STOP("STRATEGY_STOP", "策略停止"),
    STRATEGY_DELETE("STRATEGY_DELETE", "策略删除"),
    STRATEGY_EDIT("STRATEGY_EDIT", "策略编辑"),
    RULE_DELETE("RULE_DELETE", "报警规则删除"),
    RULE_EDIT("RULE_EDIT", "报警规则编辑"),
    STRATEGY_ADD("STRATEGY_ADD", "策略添加"),
    ALERT_GROUP_ADD("ALERT_GROUP_ADD", "通知组创建"),
    ALERT_GROUP_EDIT("ALERT_GROUP_EDIT", "通知组编辑"),
    ;
    private String action;
    private String desc;

    OperLogAction(String action, String desc){
        this.action = action;
        this.desc = desc;
    }

    public String getAction() {
        return action;
    }

    public String getDesc() {
        return desc;
    }

    @Override
    public String toString() {
        return "OperLogAction{" +
                "action='" + action + '\'' +
                ", desc='" + desc + '\'' +
                '}';
    }
}
