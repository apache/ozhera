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
package org.apache.ozhera.log.manager.bootstrap;

import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import org.junit.Before;
import org.junit.Test;

public class LogStoragePluginTest {

    @Before
    public void beforeFunc() {
//        Ioc.ins().init("com.xiaomi");
    }

    @Test
    public void init() {
        EsService cn = (EsService)Ioc.ins().getBean("CN");
        EsService info = (EsService)Ioc.ins().getBean("INFO");
        System.out.println("");
    }
}