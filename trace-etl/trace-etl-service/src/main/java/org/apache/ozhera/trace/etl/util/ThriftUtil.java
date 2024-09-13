/*
 * Copyright (C) 2020 Xiaomi Corporation
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
package org.apache.ozhera.trace.etl.util;

import com.xiaomi.hera.tspandata.TAttributeType;
import com.xiaomi.hera.tspandata.TValue;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

public class ThriftUtil {

    public static final TProtocolFactory PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    public static String getStringValue(TValue value, TAttributeType type){
        switch (type){
            case DOUBLE:
                return String.valueOf(value.getDoubleValue());
            case LONG:
                return String.valueOf(value.getLongValue());
            case BOOLEAN:
                return String.valueOf(value.isBoolValue());
            default:
                return value.getStringValue();
        }
    }
}
