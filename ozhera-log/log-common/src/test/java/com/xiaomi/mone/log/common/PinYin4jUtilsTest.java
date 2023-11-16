package com.xiaomi.mone.log.common;

import com.xiaomi.mone.log.utils.PinYin4jUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/10 16:07
 */
@Slf4j
public class PinYin4jUtilsTest {

    @Test
    public void testPinYin() {
        String chineseStr = "测试logStore";
        String enStr = PinYin4jUtils.getAllPinyin(chineseStr);
        Assert.assertEquals("ceshilogStore", enStr);
    }

    @Test
    public void testPinYinEmpty() {
        String chineseStr = "";
        String enStr = PinYin4jUtils.getAllPinyin(chineseStr);
        Assert.assertEquals("", enStr);
    }

    @Test
    public void testFlatMap() {
        String keyList = "timestamp:1,level:1,traceId:1,threadName:1,className:1,line:1,methodName:1,message:1,logstore:3,logsource:3,mqtopic:3,mqtag:3,logip:3,tail:3,linenumber:3";
        String columnTypeList = "date,keyword,keyword,text,text,keyword,keyword,keyword,keyword,keyword,keyword,keyword,keyword,keyword,long";

        List<String> collect = Arrays.stream(keyList.split(","))
                .flatMap(s -> Stream.of(s.split(":")[0]))
                .collect(Collectors.toList());
        Assert.assertNotNull(collect);
    }

    @Test
    public void testLongMax(){
        AtomicLong sendMsgNumber = new AtomicLong(0);
        sendMsgNumber.addAndGet(Long.MAX_VALUE);
        long l = sendMsgNumber.addAndGet(10032323);
        long l1 = 1000 % l;
        System.out.println(l1);
    }
}
