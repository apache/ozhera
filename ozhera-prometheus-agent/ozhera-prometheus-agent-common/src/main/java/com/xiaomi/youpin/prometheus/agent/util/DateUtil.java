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
