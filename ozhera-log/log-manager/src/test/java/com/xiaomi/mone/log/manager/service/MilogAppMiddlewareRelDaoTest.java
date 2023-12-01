package com.xiaomi.mone.log.manager.service;

import com.google.gson.Gson;
import com.xiaomi.mone.log.manager.dao.MilogAppMiddlewareRelDao;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static com.xiaomi.mone.log.manager.common.utils.ManagerUtil.getConfigFromNanos;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/12/1 17:30
 */
@Slf4j
public class MilogAppMiddlewareRelDaoTest {

    private MilogAppMiddlewareRelDao milogAppMiddlewareRelDao;
    private Gson gson;

    @Before
    public void init() {
        getConfigFromNanos();
        Ioc.ins().init("com.xiaomi.mone", "com.xiaomi.youpin");
        milogAppMiddlewareRelDao = Ioc.ins().getBean(MilogAppMiddlewareRelDao.class);
        gson = new Gson();
    }

    @Test
    public void testLike() {
        String topic = "common_mq_miLog_second";
        Integer count = milogAppMiddlewareRelDao.queryCountByTopicName(topic);
        log.info("res:{}", count);
    }
}
