package com.xiaomi.mone.log.stream.job.extension;

import com.xiaomi.mone.log.model.LogtailConfig;
import com.xiaomi.mone.log.model.SinkConfig;

import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/8/20 14:40
 */
public interface StreamCommonExtension {
    String dataPreProcess(String data);

    Boolean checkUniqueMarkExists(String uniqueMark, Map<String, Map<Long, String>> config);

    Map<Long, String> getConfigMapByUniqueMark(Map<String, Map<Long, String>> config, String uniqueMark);

    Boolean preCheckTaskExecution(SinkConfig sinkConfig, LogtailConfig logTailConfig, Long logSpaceId);
}
