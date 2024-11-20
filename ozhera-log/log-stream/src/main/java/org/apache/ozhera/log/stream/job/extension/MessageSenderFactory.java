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
package org.apache.ozhera.log.stream.job.extension;

import com.xiaomi.mone.es.EsProcessor;
import org.apache.ozhera.log.api.enums.LogStorageTypeEnum;
import org.apache.ozhera.log.stream.job.SinkJobConfig;
import org.apache.ozhera.log.stream.job.extension.impl.DorisMessageSender;
import org.apache.ozhera.log.stream.job.extension.impl.EsMessageSender;
import org.apache.ozhera.log.stream.job.extension.impl.RocketMqMessageProduct;
import org.apache.ozhera.log.stream.plugin.es.EsPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import static org.apache.ozhera.log.common.Constant.GSON;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/14 15:09
 */
@Slf4j
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
        if (StringUtils.isEmpty(index)) {
            log.error("es index is null,sinkJobConfig:{}", GSON.toJson(sinkJobConfig));
            throw new RuntimeException("es index is null");
        }
        EsMessageSender esMessageSender = new EsMessageSender(index, mqMessageProduct);
        EsProcessor esProcessor = EsPlugin.getEsProcessor(sinkJobConfig.getStorageInfo(), esMessageSender::compensateSend);
        esMessageSender.setEsProcessor(esProcessor);
        return esMessageSender;
    }

    private static MessageSender getDorisMessageSender(SinkJobConfig sinkJobConfig, MqMessageProduct mqMessageProduct) {
        return new DorisMessageSender(sinkJobConfig.getIndex(), mqMessageProduct, sinkJobConfig.getStorageInfo(), sinkJobConfig.getColumnList());
    }
}
