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

package org.apache.ozhera.prometheus.agent.enums;

import lombok.ToString;

@ToString
public enum ScrapeJobStatusEnum implements Base {
    PENDING(0, "pending"),
    SUCCESS(1, "success"),
    DELETE(2, "delete"),
    ALL(3, "all"),
    DONE(4, "done"),
    ;
    private Integer code;
    private String desc;

    ScrapeJobStatusEnum(Integer Code, String desc) {
        this.code = Code;
        this.desc = desc;
    }

    public static final ScrapeJobStatusEnum getEnum(Integer code) {
        if (code == null) {
            return null;
        }
        for (ScrapeJobStatusEnum jobStatus : ScrapeJobStatusEnum.values()) {
            if (code.equals(jobStatus.code)) {
                return jobStatus;
            }
        }
        return null;
    }


    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}