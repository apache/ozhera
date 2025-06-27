package org.apache.ozhera.log.agent.extension;

import org.apache.commons.collections.CollectionUtils;
import org.apache.ozhera.log.agent.common.HashUtil;
import org.apache.ozhera.log.agent.extension.nacos.AppPartitionConfigService;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2025/6/10 15:54
 */
public class TopAppPartitioner {

    private static AppPartitionConfigService appPartitionConfigService = AppPartitionConfigService.ins();

    public static int getMQNumberByAppName(String appName, int totalNum, int defaultNum) {
        if (appPartitionConfigService.containsTopicApp(appName)) {
            List<Integer> topPartitionNumberForApp = appPartitionConfigService.getTopPartitionNumberForApp(appName);
            if (CollectionUtils.isNotEmpty(topPartitionNumberForApp) && topPartitionNumberForApp.size() == 2) {
                return ThreadLocalRandom.current().nextInt(topPartitionNumberForApp.get(0), topPartitionNumberForApp.get(1) + 1);
            }
        }
        Integer partitionNumber = appPartitionConfigService.getPartitionNumberForApp(appName);
        if (null != partitionNumber) {
            int key = ThreadLocalRandom.current().nextInt(partitionNumber);
            appName = String.format("p%s%s", key, appName);
        } else {
            int key = ThreadLocalRandom.current().nextInt(defaultNum);
            appName = String.format("p%s%s", key, appName);
        }
        return HashUtil.consistentHash(appName, totalNum);
    }

}
