package com.xiaomi.mone.log.api;

import com.xiaomi.mone.log.api.enums.LogStorageTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/10 14:35
 */
@Slf4j
public class LogStorageTypeEnumTest {

    @Test
    public void queryByNameTest() {
        String name = "doris";
        LogStorageTypeEnum storageTypeEnum = LogStorageTypeEnum.queryByName(name);
        Assert.assertNotNull(storageTypeEnum);
        log.info("result:{}", storageTypeEnum);
    }
}
