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

/**
 * @author zhangping17
 */

public enum ModeEnum {
    // TODO 扩展fixed, percent
    ANY(1, "any"),
    ALL(2, "all"),
    APPOINT(3, "appoint");

    private int type;
    private String typeName;

    ModeEnum(int type, String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public int type() {
        return this.type;
    }

    public String typeName() {
        return this.typeName;
    }


    public static ModeEnum fromType(Integer type) {
        for (ModeEnum routeType : ModeEnum.values()) {
            if (null == type) {
                return null;
            }
            if (routeType.type == type) {
                return routeType;
            }
        }
        return ANY;
    }
}
