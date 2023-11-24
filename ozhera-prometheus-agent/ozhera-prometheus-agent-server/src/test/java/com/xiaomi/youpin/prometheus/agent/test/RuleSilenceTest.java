package com.xiaomi.youpin.prometheus.agent.test;

/**
 * @author zhangxiaowei6
 * @Date 2023/10/17 16:40
 **/

import com.xiaomi.youpin.prometheus.agent.Impl.SilenceDao;
import com.xiaomi.youpin.prometheus.agent.bootstrap.PrometheusAgentBootstrap;
import com.xiaomi.youpin.prometheus.agent.entity.RuleSilenceEntity;
import com.xiaomi.youpin.prometheus.agent.enums.RuleSilenceStatusEnum;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.util.Date;
import java.util.TimeZone;

public class RuleSilenceTest {
    @Test
    public void insertSilenceDb() {
        RuleSilenceEntity entity = new RuleSilenceEntity();
        entity.setUuid("uuid");
        entity.setPromCluster("open-source");
        entity.setStatus(RuleSilenceStatusEnum.SUCCESS.getDesc());
        entity.setAlertId("123");
        entity.setStartTime(new Date());
        Date endTime = new Date();
        endTime.setTime(System.currentTimeMillis() + 2 * 3600 * 1000);
        entity.setEndTime(endTime);
        entity.setCreatedTime(new Date());
        entity.setUpdatedTime(new Date());
        entity.setComment("Hera silence");
        entity.setCreatedBy("xxx");
       // Long silenceDbId = dao.CreateSilence(entity);
       // System.out.println("db insert id:" + silenceDbId);
    }
}
