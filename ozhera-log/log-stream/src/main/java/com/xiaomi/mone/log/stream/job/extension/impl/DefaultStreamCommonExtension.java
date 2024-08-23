package com.xiaomi.mone.log.stream.job.extension.impl;

import com.xiaomi.mone.log.model.LogtailConfig;
import com.xiaomi.mone.log.model.SinkConfig;
import com.xiaomi.mone.log.stream.job.extension.StreamCommonExtension;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.xiaomi.mone.log.stream.common.LogStreamConstants.DEFAULT_COMMON_STREAM_EXTENSION;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/8/20 14:45
 */
@Service(name = DEFAULT_COMMON_STREAM_EXTENSION)
@Slf4j
public class DefaultStreamCommonExtension implements StreamCommonExtension {
    @Override
    public String dataPreProcess(String data) {
        return data;
    }

    public Boolean checkUniqueMarkExists(String uniqueMark, Map<String, Map<Long, String>> config) {
        return config.containsKey(uniqueMark);
    }

    public Map<Long, String> getConfigMapByUniqueMark(Map<String, Map<Long, String>> config, String uniqueMark) {
        return config.get(uniqueMark);
    }

    @Override
    public Boolean preCheckTaskExecution(SinkConfig sinkConfig, LogtailConfig logTailConfig, Long logSpaceId) {
        return true;
    }
}
