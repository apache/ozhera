package com.xiaomi.mone.log.agent.common;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 *
 * @description
 * @version 1.0
 * @author wtt
 * @date 2024/7/11 16:55
 *
 */
@Slf4j
public class ExecutorUtilTest extends TestCase {

    public void testScheduleAtFixedRate() throws InterruptedException {

        ExecutorUtil.scheduleAtFixedRate(() -> {
            log.info("I am a task");
        }, 10, 30, java.util.concurrent.TimeUnit.SECONDS);

        ExecutorUtil.submit(() -> {
            while(true){
                log.info("I am a submit task");
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread.sleep(TimeUnit.MINUTES.toMillis(2));
    }

}