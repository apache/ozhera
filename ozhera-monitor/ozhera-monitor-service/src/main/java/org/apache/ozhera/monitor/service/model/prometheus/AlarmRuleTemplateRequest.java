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

package org.apache.ozhera.monitor.service.model.prometheus;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.ozhera.monitor.dao.model.AppAlarmRule;
import org.apache.ozhera.monitor.dao.model.AppAlarmRuleTemplate;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2021/9/14 9:09 AM
 */
@Data
public class AlarmRuleTemplateRequest implements Serializable {

    private AppAlarmRuleTemplate template;
    private List<AppAlarmRule> alarmRules;

    public static void main(String[] args) {
        AppAlarmRuleTemplate template = new AppAlarmRuleTemplate();
        template.setName("testTemplate");
        template.setRemark("备注1");

        AppAlarmRule rule = new AppAlarmRule();
        rule.setAlert("cupUseRate");
        rule.setPriority("P0");
        JsonObject alertTeam1 = new JsonObject();
        alertTeam1.addProperty("id",2);
        alertTeam1.addProperty("type","oncall");
        alertTeam1.addProperty("name","falcon-oncal");
        rule.setAlertTeam(alertTeam1.toString());
        rule.setDataCount(3);
        rule.setOp(">");
        rule.setValue(90f);
        rule.setSendInterval("1h");

        AppAlarmRule rule1 = new AppAlarmRule();
        rule1.setAlert("memUseRate");
        rule1.setPriority("P0");
        JsonObject alertTeam = new JsonObject();
        alertTeam.addProperty("id",2);
        alertTeam.addProperty("type","oncall");
        alertTeam.addProperty("name","falcon-oncal");
        rule1.setAlertTeam(alertTeam.toString());
        rule1.setOp(">");
        rule1.setValue(90f);
        rule1.setSendInterval("1h");

        List<AppAlarmRule> list = new ArrayList<>();
        list.add(rule);
        list.add(rule1);

        AlarmRuleTemplateRequest request = new AlarmRuleTemplateRequest();
        request.setAlarmRules(list);
        request.setTemplate(template);

        System.out.println(new Gson().toJson(request));
    }
}
