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
package org.apache.ozhera.log.stream;

import org.apache.ozhera.log.stream.sink.SinkChain;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/8/7 15:45
 */
@Slf4j
public class TestSinkChain {

    private SinkChain sinkChain;

    @Before
    public void init() {
//        getConfigFromNacos();
        Ioc.ins().init("com.xiaomi.mone.log.stream", "com.xiaomi.youpin.docean");
        sinkChain = Ioc.ins().getBean(SinkChain.class);
    }

    @Test
    public void testChain() {
        Map<String, Object> data = new HashMap();
//        sinkChain.execute(data);
    }
}
