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
package com.xiaomi.youpin.prometheus.agent.test;

import com.xiaomi.youpin.prometheus.agent.util.DateUtil;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

public class TimeTest {

    @Test
    public void testStartTimeAndEndTime() {
        System.out.println(ValidateTime(1682975833, 1682975832));
    }

    private String ValidateTime(long startTime, long endTime) {
        Timestamp sTimeStamp = new Timestamp(startTime);
        Timestamp eTimeStamp = new Timestamp(endTime);
        Timestamp nowTimeStamp = new Timestamp(System.currentTimeMillis() / 1000);
        if (sTimeStamp.equals(0) || eTimeStamp.equals(0)) {
            return "invalid zero start timestamp ro end timestamp";
        }
        if (eTimeStamp.before(nowTimeStamp)) {
            return "end time can not be in the past";
        }
        if (eTimeStamp.before(sTimeStamp)) {
            return "end time must not be before start time";
        }
        return "";
    }

    @Test
    public void testTime() {
        String time = "2023-11-16T02:18:33.633Z";
        System.out.println(DateUtil.ISO8601UTCTOTimeStamp(time));
        System.out.println(DateUtil.ISO8601UTCTOCST(time));
    }
}
