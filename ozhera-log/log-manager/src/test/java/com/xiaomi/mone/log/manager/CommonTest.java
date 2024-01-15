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
package com.xiaomi.mone.log.manager;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/7/7 17:28
 */
@Slf4j
public class CommonTest {

    @Test
    public void testCollection() {
        List<String> list = Lists.newArrayList("zhangsan", "lisi", "test", "john");
        List<String> otherAdmins = CollectionUtil.sub(list, 1, list.size());
        log.info("result:{}", otherAdmins);
    }
}
