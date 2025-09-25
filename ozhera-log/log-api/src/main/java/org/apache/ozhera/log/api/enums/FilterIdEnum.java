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
package org.apache.ozhera.log.api.enums;

import lombok.Getter;

/**
 * @author wtt
 * @date 2025/9/15 9:58
 * @version 1.0
 */
@Getter
public enum FilterIdEnum {
    FILTER_SPACE_ID(1, "space"),
    FILTER_STORE_ID(2, "store"),
    FILTER_TAIL_ID(3, "tail");
    final int code;
    final String desc;

    FilterIdEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
