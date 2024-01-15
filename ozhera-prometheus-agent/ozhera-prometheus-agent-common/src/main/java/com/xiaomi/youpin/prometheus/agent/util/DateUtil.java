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
package com.xiaomi.youpin.prometheus.agent.util;

import lombok.SneakyThrows;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtil {


    @SneakyThrows
    public static String Time2YYMMdd(String inputDate) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss",Locale.CHINA);
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

    @SneakyThrows
    public static long ISO8601UTCTOTimeStamp(String utcTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = sdf.parse(utcTime);
        return date.getTime();
    }

    @SneakyThrows
    public static String ISO8601UTCTOCST(String utcTime) {
        SimpleDateFormat sdfUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdfUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = sdfUTC.parse(utcTime);

        SimpleDateFormat sdfCST = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdfCST.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String cstTime = sdfCST.format(date);

        return cstTime;
    }

}
