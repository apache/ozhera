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

package org.apache.ozhera.prometheus.agent.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

@Slf4j
public class DateUtil {


    @SneakyThrows
    public static String Time2YYMMdd(String inputDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss", Locale.CHINA);
        Date date = inputFormat.parse(inputDate);
        String outputDate = outputFormat.format(date);
        return outputDate;
    }

    @SneakyThrows
    public static String TimeStampToISO8601UTC(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd'T'HH:MM:ss.sss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String utcTime = sdf.format(new Date(timeStamp));
        return utcTime;
    }

    public static long ISO8601UTCTOTimeStamp(String utcTime) {
        try {
            // The parsing time string is Instant object
            Instant instant = Instant.parse(utcTime);
            // fetch timestamp and return
            return instant.toEpochMilli();
        } catch (DateTimeParseException e) {
            // if parsing fail，Try to remove the last decimal point and the millisecond portion before parsing
            int lastDotIndex = utcTime.lastIndexOf('.');
            if (lastDotIndex != -1) {
                String dateTimeStringWithoutMillis = utcTime.substring(0, lastDotIndex) + "Z";
                try {
                    Instant instant = Instant.parse(dateTimeStringWithoutMillis);
                    return instant.toEpochMilli();
                } catch (DateTimeParseException ex) {
                    log.info("parse time string fail：" + ex.getMessage());
                }
            }
            // if parsing fail，If an error message is displayed and -1 is returned, the conversion failed
            log.info("parse time string fail：" + e.getMessage());
            return -1;
        }
    }

    public static String ISO8601UTCTOCST(String utcTime) {
        try {
            Instant instant = Instant.parse(utcTime);
            // to cst
            ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("Asia/Shanghai"));
            LocalDateTime localDateTime = zonedDateTime.toLocalDateTime();
            return String.valueOf(localDateTime);
        } catch (DateTimeParseException e) {
            log.info("parse time string fail ：" + e.getMessage());
            return null;
        }
    }

}