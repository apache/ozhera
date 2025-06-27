package org.apache.ozhera.log.agent.extension.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.data.push.common.SafeRun;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.agent.common.ExecutorUtil;
import org.apache.ozhera.log.common.Config;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.GSON;


/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/9/8 15:25
 */
@Slf4j
public class AppPartitionConfigService {

    private NacosConfigUtil nacosUtil;

    private final AtomicReference<Map<String, Integer>> appPartitionMapRef = new AtomicReference<>(new ConcurrentHashMap<>());
    private final AtomicReference<Map<String, List<Integer>>> topAppPartitionMapRef = new AtomicReference<>(new ConcurrentHashMap<>());

    private static final String APP_PARTITION_KEY = "app_partition_key";
    private static final String TOP_APP_PARTITION_KEY = "top_app_partition_key";

    @Getter
    private volatile TopicInfo topicInfo;

    private static final class LazyHolder {
        private static final AppPartitionConfigService INSTANCE = new AppPartitionConfigService();
    }

    public static AppPartitionConfigService ins() {
        return LazyHolder.INSTANCE;
    }

    private AppPartitionConfigService() {
        init();
    }

    private void init() {
        if (!"true".equalsIgnoreCase(Config.ins().get("app_partition_key_switch", ""))) {
            log.info("App partition config switch is off.");
            return;
        }

        try {
            nacosUtil = new NacosConfigUtil(Config.ins().get("nacosAddr", ""));
            ExecutorUtil.scheduleAtFixedRate(this::syncConfigSafe, 0, 1, TimeUnit.MINUTES);
        } catch (NacosException e) {
            log.error("Failed to initialize NacosConfigUtil", e);
        }
    }

    private void syncConfigSafe() {
        SafeRun.run(this::syncConfig);
    }

    private void syncConfig() {
        updateAppPartitionConfig();
        updateTopAppPartitionConfig();
    }

    private void updateAppPartitionConfig() {
        String dataId = Config.ins().get(APP_PARTITION_KEY, APP_PARTITION_KEY);
        try {
            String configJson = nacosUtil.getConfig(dataId);
            if (StringUtils.isNotEmpty(configJson)) {
                List<AppPartitionConfig> configList = GSON.fromJson(configJson, new TypeToken<List<AppPartitionConfig>>() {
                }.getType());
                Map<String, Integer> updatedMap = configList.stream().collect(Collectors.toMap(AppPartitionConfig::getAppName, AppPartitionConfig::getPartitionNum, (v1, v2) -> v2));
                appPartitionMapRef.set(updatedMap);
                log.info("Updated appPartitionMap: {}", GSON.toJson(updatedMap));
            }
        } catch (Exception e) {
            log.error("Failed to update AppPartitionConfig from Nacos, dataId: {}", dataId, e);
        }
    }

    private void updateTopAppPartitionConfig() {
        String dataId = Config.ins().get(TOP_APP_PARTITION_KEY, TOP_APP_PARTITION_KEY);
        try {
            String configJson = nacosUtil.getConfig(dataId);
            if (StringUtils.isNotEmpty(configJson)) {
                TopAppPartitionConfig config = GSON.fromJson(configJson, TopAppPartitionConfig.class);
                if (config != null) {
                    topicInfo = config.getTopicInfo();
                    Map<String, List<Integer>> updatedMap = config.getConfigs().stream().collect(Collectors.toMap(TopPerAppPartitionConfig::getAppName, TopPerAppPartitionConfig::getHashRange, (v1, v2) -> v2));
                    topAppPartitionMapRef.set(updatedMap);
                    log.info("Updated topAppPartitionMap: {}", GSON.toJson(updatedMap));
                }
            }
        } catch (Exception e) {
            log.error("Failed to update TopAppPartitionConfig from Nacos, dataId: {}", dataId, e);
        }
    }

    public Integer getPartitionNumberForApp(String appName) {
        return appPartitionMapRef.get().get(appName);
    }

    public List<Integer> getTopPartitionNumberForApp(String appName) {
        return topAppPartitionMapRef.get().get(appName);
    }

    public boolean containsTopicApp(String appName) {
        return topAppPartitionMapRef.get().containsKey(appName);
    }

    @Data
    public static class AppPartitionConfig {
        private String appName;
        private Integer partitionNum;
    }

    @Data
    public static class TopAppPartitionConfig {
        private TopicInfo topicInfo;
        private List<TopPerAppPartitionConfig> configs;
    }

    @Data
    public static class TopicInfo {
        private String topic;
        private String ak;
        private String sk;
        // This field corresponds to the clusterInfo in OutPut, which represents the MQ connection information.
        private String endpoint;
    }

    @Data
    public static class TopPerAppPartitionConfig {
        private String appName;
        private List<Integer> hashRange;
    }
}
