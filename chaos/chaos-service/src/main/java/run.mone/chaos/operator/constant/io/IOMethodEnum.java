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
package run.mone.chaos.operator.constant.io;

import java.util.Objects;

public enum IOMethodEnum {
    WRITE(1, "write"),
    READ(2, "read"),
    ;

    private int type;
    private String typeName;

    IOMethodEnum(int type, String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public int type() {
        return this.type;
    }

    public String typeName() {
        return this.typeName;
    }

    public static boolean IsContainByTypeName(String typeName) {
        for (IOMethodEnum routeType : IOMethodEnum.values()) {
            if (Objects.equals(routeType.typeName, typeName)) {
                return true;
            }
        }
        return false;
    }
}
