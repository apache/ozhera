package com.xiaomi.mone.log.stream.job.extension.kafka;

import com.xiaomi.mone.log.stream.common.LogStreamConstants;
import com.xiaomi.mone.log.stream.job.SinkJobConfig;
import com.xiaomi.mone.log.stream.job.extension.MqMessagePostProcessing;
import com.xiaomi.youpin.docean.anno.Service;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/12/1 18:20
 */
@Service(name = "kafka" + LogStreamConstants.postProcessingProviderBeanSuffix)
public class KafkaMqMessagePostProcessing implements MqMessagePostProcessing {
    @Override
    public void postProcessing(SinkJobConfig sinkJobConfig, String message) {

    }
}
