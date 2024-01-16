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
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.xiaomi.hera.tspandata;

import org.apache.thrift.TEnum;

public enum TAttributeType implements TEnum {
    STRING(1),
    BOOLEAN(2),
    LONG(3),
    DOUBLE(4),
    STRING_ARRAY(5),
    BOOLEAN_ARRAY(6),
    LONG_ARRAY(7),
    DOUBLE_ARRAY(8);

    private final int value;

    private TAttributeType(int var3) {
        this.value = var3;
    }

    public int getValue() {
        return this.value;
    }

    public static TAttributeType findByValue(int var0) {
        switch(var0) {
            case 1:
                return STRING;
            case 2:
                return BOOLEAN;
            case 3:
                return LONG;
            case 4:
                return DOUBLE;
            case 5:
                return STRING_ARRAY;
            case 6:
                return BOOLEAN_ARRAY;
            case 7:
                return LONG_ARRAY;
            case 8:
                return DOUBLE_ARRAY;
            default:
                return null;
        }
    }
}
