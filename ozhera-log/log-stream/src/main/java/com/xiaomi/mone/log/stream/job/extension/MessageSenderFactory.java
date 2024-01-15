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
package com.xiaomi.mone.log.stream.job.extension;

import com.xiaomi.mone.es.EsProcessor;
import com.xiaomi.mone.log.api.enums.LogStorageTypeEnum;
import com.xiaomi.mone.log.stream.job.SinkJobConfig;
import com.xiaomi.mone.log.stream.job.extension.impl.DorisMessageSender;
import com.xiaomi.mone.log.stream.job.extension.impl.EsMessageSender;
import com.xiaomi.mone.log.stream.job.extension.impl.RocketMqMessageProduct;
import com.xiaomi.mone.log.stream.plugin.es.EsPlugin;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/14 15:09
 */
public class MessageSenderFactory {

    public static MessageSender getMessageSender(SinkJobConfig sinkJobConfig) {
        MqMessageProduct mqMessageProduct = new RocketMqMessageProduct();
        String storageType = sinkJobConfig.getStorageType();
        LogStorageTypeEnum storageTypeEnum = LogStorageTypeEnum.queryByName(storageType);
        if (null == storageTypeEnum) {
            return getEsMessageSender(sinkJobConfig, mqMessageProduct);
        }
        switch (storageTypeEnum) {
            case ELASTICSEARCH:
                return getEsMessageSender(sinkJobConfig, mqMessageProduct);
            case DORIS:
                return getDorisMessageSender(sinkJobConfig, mqMessageProduct);
            default:
                return null;
        }
    }

    private static MessageSender getEsMessageSender(SinkJobConfig sinkJobConfig, MqMessageProduct mqMessageProduct) {
        String index = sinkJobConfig.getIndex();
        EsMessageSender esMessageSender = new EsMessageSender(index, mqMessageProduct);
        EsProcessor esProcessor = EsPlugin.getEsProcessor(sinkJobConfig.getStorageInfo(), mqMessageDTO -> esMessageSender.compensateSend(mqMessageDTO));
        esMessageSender.setEsProcessor(esProcessor);
        return esMessageSender;
    }

    private static MessageSender getDorisMessageSender(SinkJobConfig sinkJobConfig, MqMessageProduct mqMessageProduct) {
        return new DorisMessageSender(sinkJobConfig.getIndex(), mqMessageProduct, sinkJobConfig.getStorageInfo(), sinkJobConfig.getColumnList());
    }
}
