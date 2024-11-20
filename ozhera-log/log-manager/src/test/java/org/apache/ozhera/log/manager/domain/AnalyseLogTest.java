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
package org.apache.ozhera.log.manager.domain;

import com.xiaomi.youpin.docean.Ioc;
import org.apache.ozhera.log.manager.model.vo.LogAnalyseDataQuery;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class AnalyseLogTest {
    private AnalyseLog analyseLog;

    @Before
    public void pushBean() {
        Ioc.ins().init("com.xiaomi");
        analyseLog = Ioc.ins().getBean(AnalyseLog.class);
    }

    @Test
    public void getData() throws IOException {
        LogAnalyseDataQuery query = new LogAnalyseDataQuery();
        query.setGraphId(1l);
        query.setStartTime(1661858725000l);
        query.setEndTime(1661862325000l);
//        LogAnalyseDataDTO data = analyseLog.getData(query);
//        System.out.println(data);
    }
}