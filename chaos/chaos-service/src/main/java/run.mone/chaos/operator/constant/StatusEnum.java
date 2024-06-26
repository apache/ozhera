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
package run.mone.chaos.operator.constant;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public enum StatusEnum {

    un_know(-99, "un_know", "未知状态"),

    init(0, "init", "初始化"),
    un_action(1, "un_action", "未执行"),
    actioning(2, "actioning", "执行中"),
    fail(3, "fail", "执行失败"),
    recovered(4, "recovered", "已恢复"),
    ;

    private int type;
    private String typeName;

    private String desc;

    StatusEnum(int type, String typeName, String desc) {
        this.type = type;
        this.typeName = typeName;
        this.desc = desc;
    }

    public int type() {
        return this.type;
    }

    public String typeName() {
        return this.typeName;
    }


    public static StatusEnum fromType(Integer type) {
        for (StatusEnum routeType : StatusEnum.values()) {
            if (routeType.type == type) {
                return routeType;
            }
        }
        return un_know;
    }

    public static Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        StatusEnum[] values = values();
        Arrays.stream(values).forEach(i -> map.put(i.desc, i.type));
        return map;
    }
}
