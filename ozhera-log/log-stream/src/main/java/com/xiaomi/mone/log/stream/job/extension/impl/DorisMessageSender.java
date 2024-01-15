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
package com.xiaomi.mone.log.stream.job.extension.impl;

import com.xiaomi.mone.log.model.StorageInfo;
import com.xiaomi.mone.log.stream.common.util.StreamUtils;
import com.xiaomi.mone.log.stream.job.compensate.MqMessageDTO;
import com.xiaomi.mone.log.stream.job.extension.MessageSender;
import com.xiaomi.mone.log.stream.job.extension.MqMessageProduct;
import lombok.extern.slf4j.Slf4j;
import run.mone.doris.DorisStreamLoad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import static com.xiaomi.mone.log.common.Constant.GSON;
import static com.xiaomi.mone.log.stream.common.util.StreamUtils.extractInfoFromJdbcUrl;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/14 15:14
 */
@Slf4j
public class DorisMessageSender implements MessageSender {

    private final String tableName;
    /**
     * Compensating message MQ queue.
     */
    private final MqMessageProduct compensateMsgProduct;

    private final List<String> columnList;

    private DorisStreamLoad dorisStreamLoad;

    private static final Integer DEFAULT_PORT = 8030;

    private String dataBaseName;

    private ExecutorService executors;

    private List<Map<String, Object>> dataList = new ArrayList<>();

    private final static Integer BATCH_SEND_SIZE = 1000;

    private ReentrantLock reentrantLock = new ReentrantLock();

    public DorisMessageSender(String tableName, MqMessageProduct compensateMsgProduct, StorageInfo storageInfo, List<String> columnList) {
        this.tableName = tableName;
        this.compensateMsgProduct = compensateMsgProduct;
        this.columnList = columnList;

        StreamUtils.JdbcInfo jdbcInfo = extractInfoFromJdbcUrl(storageInfo.getAddr());
        Integer port = null != storageInfo.getPort() ? storageInfo.getPort() : DEFAULT_PORT;
        this.dataBaseName = jdbcInfo.getDbName();

        this.dorisStreamLoad = new DorisStreamLoad(jdbcInfo.getIp(), storageInfo.getUser(), storageInfo.getPwd(), port);
        this.executors = Executors.newVirtualThreadPerTaskExecutor();

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleAtFixedRate(this::flush, 1000, 3000, TimeUnit.MILLISECONDS);
    }

    private void flush() {
        if (reentrantLock.tryLock()) {
            try {
                dorisStreamLoad.sendData(dataBaseName, tableName, columnList, dataList);
                dataList.clear();
            } catch (Exception e) {
                log.error("flush data error", e);
            } finally {
                reentrantLock.unlock();
            }
        }
    }


    @Override
    public Boolean send(Map<String, Object> data) throws Exception {
        try {
            log.info("dataBaseName:{},tableName:{},columnList:{},data:{}", dataBaseName, tableName, columnList, GSON.toJson(data));
            reentrantLock.lock();
            dataList.add(data);

            if (dataList.size() > BATCH_SEND_SIZE) {
                List<Map<String, Object>> subList = dataList.subList(0, BATCH_SEND_SIZE);
                dorisStreamLoad.sendData(dataBaseName, tableName, columnList, subList);
                subList.clear();
            }
        } finally {
            reentrantLock.unlock();
        }
        return true;
    }

    @Override
    public boolean compensateSend(MqMessageDTO compensateMsg) {
        if (null != compensateMsgProduct) {
            compensateMsgProduct.product(compensateMsg);
            return true;
        }

        return false;
    }

}
