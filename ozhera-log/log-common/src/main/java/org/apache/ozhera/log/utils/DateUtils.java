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
package org.apache.ozhera.log.utils;

import cn.hutool.core.date.CalendarUtil;
import cn.hutool.core.date.DateException;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.format.DateParser;
import cn.hutool.core.lang.PatternPool;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static cn.hutool.core.date.DateUtil.*;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/12/29 12:40
 */
public class DateUtils extends CalendarUtil {

    public static long dayms = 86400000L;
    private final static String[] wtb = {
            "sun", "mon", "tue", "wed", "thu", "fri", "sat",
            "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec",
            "gmt", "ut", "utc", "est", "edt", "cst", "cdt", "mst", "mdt", "pst", "pdt"
    };

    public static String getDaysAgo(int n) {
        return new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis() - n * dayms);
    }

    public static String timeStamp2Date(String millSeconds, String format) {
        if (StringUtils.isBlank(millSeconds)) {
            return "";
        }
        if (format == null || format.isEmpty()) format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(millSeconds)));
    }

    public static String getTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return dateTimeFormatter.format(LocalDateTime.now());
    }

    public static String getTime(int plusDays) {
        LocalDate today = LocalDate.now();

        LocalDate tomorrow = today.plusDays(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

        return tomorrow.format(formatter);
    }

    public static Long getThisDayFirstMillisecond(String thisDay) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = format.parse(thisDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    private static String normalize(CharSequence dateStr) {
        if (StrUtil.isBlank(dateStr)) {
            return StrUtil.str(dateStr);
        }

        final List<String> dateAndTime = StrUtil.splitTrim(dateStr, ' ');
        final int size = dateAndTime.size();
        if (size < 1 || size > 2) {
            return StrUtil.str(dateStr);
        }

        final StringBuilder builder = StrUtil.builder();

        String datePart = dateAndTime.get(0).replaceAll("[/.年月]", "-");
        datePart = StrUtil.removeSuffix(datePart, "日");
        builder.append(datePart);

        if (size == 2) {
            builder.append(' ');
            String timePart = dateAndTime.get(1).replaceAll("[时分秒]", ":");
            timePart = StrUtil.removeSuffix(timePart, ":");
            timePart = timePart.replace(',', '.');
            builder.append(timePart);
        }

        return builder.toString();
    }

    public static DateTime parse(CharSequence dateStr, DateParser parser) {
        return new DateTime(dateStr, parser);
    }

    public static DateTime parse(CharSequence dateCharSequence) {
        if (StrUtil.isBlank(dateCharSequence)) {
            return null;
        }
        String dateStr = dateCharSequence.toString();
        dateStr = StrUtil.removeAll(dateStr.trim(), '日', '秒');
        int length = dateStr.length();
        if (NumberUtil.isNumber(dateStr)) {
            if (length == DatePattern.PURE_DATETIME_PATTERN.length()) {
                return parse(dateStr, DatePattern.PURE_DATETIME_FORMAT);
            } else if (length == DatePattern.PURE_DATETIME_MS_PATTERN.length()) {
                return parse(dateStr, DatePattern.PURE_DATETIME_MS_FORMAT);
            } else if (length == DatePattern.PURE_DATE_PATTERN.length()) {
                return parse(dateStr, DatePattern.PURE_DATE_FORMAT);
            } else if (length == DatePattern.PURE_TIME_PATTERN.length()) {
                return parse(dateStr, DatePattern.PURE_TIME_FORMAT);
            }
        } else if (ReUtil.isMatch(PatternPool.TIME, dateStr)) {
            return parseTimeToday(dateStr);
        } else if (StrUtil.containsAnyIgnoreCase(dateStr, wtb)) {
            return parseCST(dateStr);
        } else if (StrUtil.contains(dateStr, 'T')) {
            // UTC时间
            return parseUTC(dateStr);
        }

        dateStr = normalize(dateStr);
//        if (ReUtil.isMatch(DatePattern.REGEX_NORM, dateStr)) {
        if (parseDateTime(dateStr).isPresent()) {
            final int colonCount = StrUtil.count(dateStr, CharUtil.COLON);
            switch (colonCount) {
                case 0:
                    return parse(dateStr, DatePattern.NORM_DATE_FORMAT);
                case 1:
                    return parse(dateStr, DatePattern.NORM_DATETIME_MINUTE_FORMAT);
                case 2:
                    final int indexOfDot = StrUtil.indexOf(dateStr, CharUtil.DOT);
                    if (indexOfDot > 0) {
                        final int length1 = dateStr.length();
                        if (length1 - indexOfDot > 4) {
                            dateStr = StrUtil.subPre(dateStr, indexOfDot + 4);
                        }
                        return parse(dateStr, DatePattern.NORM_DATETIME_MS_FORMAT);
                    }
                    return parse(dateStr, DatePattern.NORM_DATETIME_FORMAT);
            }
        }
        throw new DateException("No format fit for date String [{}] !", dateStr);
    }

    private static final List<DateTimeFormatter> FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    public static Optional<LocalDateTime> parseDateTime(String dateStr) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            try {
                return Optional.of(LocalDateTime.parse(dateStr, formatter));
            } catch (DateTimeParseException ignored) {
            }
        }
        return Optional.empty();
    }
}
