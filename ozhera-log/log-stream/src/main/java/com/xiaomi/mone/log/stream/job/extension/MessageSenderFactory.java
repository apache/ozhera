package com.xiaomi.mone.log.stream.job.extension;

import com.xiaomi.mone.es.EsProcessor;
import com.xiaomi.mone.log.api.enums.LogStorageTypeEnum;
import com.xiaomi.mone.log.stream.job.SinkJobConfig;
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
        if (null == sinkJobConfig) {
            return getEsMessageSender(sinkJobConfig, mqMessageProduct);
        }
        switch (storageTypeEnum) {
            case ELASTICSEARCH:
                return getEsMessageSender(sinkJobConfig, mqMessageProduct);
            case DORIS:
//                return getDorisMessageSender(sinkJobConfig, mqMessageProduct);
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

//    private static MessageSender getDorisMessageSender(SinkJobConfig sinkJobConfig, MqMessageProduct mqMessageProduct) {
//        List<String> keyListSlice = IndexUtils.getKeyListSlice(sinkJobConfig.getKeyList());
//        return new DorisMessageSender(sinkJobConfig.getIndex(), mqMessageProduct, sinkJobConfig.getStorageInfo(), keyListSlice);
//    }
}
