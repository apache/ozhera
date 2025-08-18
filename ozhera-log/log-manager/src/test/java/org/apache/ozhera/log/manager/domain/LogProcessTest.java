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
import org.apache.ozhera.log.api.model.vo.UpdateLogProcessCmd;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class LogProcessTest {
    private LogProcess logProcess;

    @Before
    public void pushBean() {
        Ioc.ins().init("com.xiaomi");
        logProcess = Ioc.ins().getBean(LogProcess.class);
    }

    @Test
    public void updateAndGetLogProcess() {
        for (int j = 0; j < 3; j++) {
            UpdateLogProcessCmd cmd = getUpdateLogProcessCmd(j);
            logProcess.updateLogProcess(cmd);
        }
//        logProcess.getAgentLogProcess("127.0.0.1").forEach(System.out::println);
//        System.out.println("======================");
//        logProcess.getAgentLogProcess("127.0.0.1").forEach(System.out::println);
//        System.out.println("======================");
//        logProcess.getAgentLogProcess("127.0.0.1").forEach(System.out::println);
    }

    @NotNull
    private static UpdateLogProcessCmd getUpdateLogProcessCmd(int j) {
        String ip = "127.0.0.1." + j;
        UpdateLogProcessCmd cmd = new UpdateLogProcessCmd();
        List<UpdateLogProcessCmd.CollectDetail> collectDetailList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UpdateLogProcessCmd.CollectDetail detail = new UpdateLogProcessCmd.CollectDetail();
            detail.setAppId(i * 1l);
            detail.setAppName("appName");
            detail.setPath("home/so/server" + i + ".log");
            collectDetailList.add(detail);
        }
        cmd.setIp(ip);
        cmd.setCollectList(collectDetailList);
        return cmd;
    }
}