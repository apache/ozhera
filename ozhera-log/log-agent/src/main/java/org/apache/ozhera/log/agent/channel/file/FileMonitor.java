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
package org.apache.ozhera.log.agent.channel.file;

import com.google.common.collect.Lists;
import com.xiaomi.mone.file.LogFile;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

import static org.apache.ozhera.log.common.Constant.GSON;

@Slf4j
@Service
public class FileMonitor implements FileWatcher {

    @Override
    public void watch(String filePattern, List<FileAlterationMonitor> monitorList, Consumer<String> consumer) {
        List<String> watchList = Lists.newArrayList(filePattern);
        //Default traverse file interval time is 10s.
        FileAlterationMonitor monitor = new FileAlterationMonitor(10000);
        log.info("agent monitor files:{}", GSON.toJson(watchList));
        for (String watch : watchList) {
            FileAlterationObserver observer = new LogFileAlterationObserver(new File(watch), File::exists);
            observer.addListener(new FileListener(consumer));
            log.info("## agent monitor file:{}, filePattern:{}", watch, filePattern);
            monitor.addObserver(observer);
        }
        try {
            monitor.start();
            log.info("## agent monitor filePattern:{} started", filePattern);
            monitorList.add(monitor);
        } catch (Exception e) {
            log.error(String.format("agent file monitor start err,monitor filePattern:%s", filePattern), e);
        }
    }

    @Override
    public void watch(LogFile logFile) {

    }

    @Override
    public void onChange() {

    }
}
