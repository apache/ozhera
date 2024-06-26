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

public enum JvmActionEnum {

    exception(0, "exception"),
    re(1, "return");

    private int type;
    private String typeName;

    JvmActionEnum(int type, String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public int type() {
        return this.type;
    }

    public String typeName() {
        return this.typeName;
    }


    public static JvmActionEnum fromType(Integer type) {
        for (JvmActionEnum routeType : JvmActionEnum.values()) {
            if (routeType.type == type) {
                return routeType;
            }
        }
        return null;
    }

    public static Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        JvmActionEnum[] values = values();
        Arrays.stream(values).forEach(i -> map.put(i.typeName(), i.type()));
        return map;
    }
}
