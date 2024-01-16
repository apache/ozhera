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
package com.xiaomi.youpin.prometheus.agent.param.alert;
import com.xiaomi.youpin.prometheus.agent.param.BaseParam;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
public class RuleAlertParam extends BaseParam {
    private String alert;
    private String cname;
    private String expr;
    private String forTime;
    private Map<String,String> labels;
    private Map<String,String> annotations;
    private String group;
    private String priority;
    private List<String> env;
    private List<String> alert_member;
    private Integer enabled;
    private String createdBy;
    private String promCluster;
    private List<String> alert_at_people;

    public boolean argCheck() {
        return true;
    }
}
