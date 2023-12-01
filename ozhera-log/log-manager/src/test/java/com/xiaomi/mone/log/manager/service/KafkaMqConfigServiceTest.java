package com.xiaomi.mone.log.manager.service;

import com.google.gson.Gson;
import com.xiaomi.mone.log.manager.model.dto.DictionaryDTO;
import com.xiaomi.mone.log.manager.model.pojo.MilogAppMiddlewareRel;
import com.xiaomi.mone.log.manager.service.impl.KafkaMqConfigService;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.xiaomi.mone.log.manager.common.utils.ManagerUtil.getConfigFromNanos;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/30 16:33
 */
@Slf4j
public class KafkaMqConfigServiceTest {

    private KafkaMqConfigService kafkaMqConfigService;

    private Gson gson;

    private String ak = "";
    private String sk = "";
    private String clusterInfo = "";

    //    @Before
    public void init() {
        getConfigFromNanos();
        Ioc.ins().init("com.xiaomi.mone", "com.xiaomi.youpin");
        kafkaMqConfigService = Ioc.ins().getBean(KafkaMqConfigService.class);
        gson = new Gson();
    }

    //    @Test
    public void testQueryTopicList() {
        List<DictionaryDTO> dictionaryDTOS = kafkaMqConfigService.queryExistsTopic(ak, sk, clusterInfo, "", "", "", "");
        dictionaryDTOS.forEach(System.out::println);
    }

    //    @Test
    public void generateTopicTest() {
        MilogAppMiddlewareRel.Config config = kafkaMqConfigService.generateConfig(ak, sk, clusterInfo, "", "", "", "", null, "test-app", "", 1233434L);
        log.info("topic result:{}", gson.toJson(config));
    }
}
