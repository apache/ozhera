package com.xiaomi.mone.log.stream;

import com.xiaomi.mone.es.EsProcessor;
import com.xiaomi.mone.log.model.StorageInfo;
import com.xiaomi.mone.log.stream.plugin.es.EsPlugin;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static com.xiaomi.mone.log.stream.common.util.StreamUtils.getConfigFromNacos;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/1/30 20:09
 */
@Slf4j
public class EsPluginTest {

    @Before
    public void init() {
        getConfigFromNacos();
        Ioc.ins().init("com.xiaomi.mone.log.stream", "com.xiaomi.youpin.docean");
    }

    @Test
    public void getEsProcessorTest() {
        Long id = 1L;
        String addr = "127.0.0.1:80";
        String user = "user";
        String pwd = "pwd";
        StorageInfo esInfo = new StorageInfo(id, addr, user, pwd);
        EsProcessor esProcessor = EsPlugin.getEsProcessor(esInfo, null);

        EsProcessor esProcessor1 = EsPlugin.getEsProcessor(esInfo, null);
        esProcessor1 = EsPlugin.getEsProcessor(esInfo, null);
        log.info("result:{}", esProcessor);
    }
}
