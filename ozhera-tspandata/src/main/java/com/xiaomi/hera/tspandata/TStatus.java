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

public enum TStatus implements TEnum {
    OK(1),
    UNSET(2),
    ERROR(3);

    private final int value;

    private TStatus(int var3) {
        this.value = var3;
    }

    public int getValue() {
        return this.value;
    }

    public static TStatus findByValue(int var0) {
        switch(var0) {
            case 1:
                return OK;
            case 2:
                return UNSET;
            case 3:
                return ERROR;
            default:
                return null;
        }
    }
}
