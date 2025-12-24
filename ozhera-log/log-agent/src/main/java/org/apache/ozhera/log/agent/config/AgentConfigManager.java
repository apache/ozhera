package org.apache.ozhera.log.agent.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

/**
 * @author wtt
 * @date 2025/12/23 14:13
 * @version 1.0
 */
@Slf4j
public class AgentConfigManager {

    private static final String AGENT_CONFIG_DATA_ID = "org.apache.ozhera.log.agent.config";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Properties CACHE = new Properties();

    private final ConfigCenter configCenter;

    public AgentConfigManager(ConfigCenter configCenter) {
        this.configCenter = configCenter;
        init();
    }

    private void init() {
        try {
            String configContent = configCenter.getConfig(AGENT_CONFIG_DATA_ID);
            refresh(configContent);

            configCenter.addListener(AGENT_CONFIG_DATA_ID, this::refresh);
        } catch (Exception e) {
            log.error("[AgentConfig] init failed", e);
        }
    }

    private synchronized void refresh(String configContent) {
        if (configContent == null || configContent.isEmpty()) {
            log.warn("[AgentConfig] empty config, skip refresh");
            return;
        }

        try {
            Properties newConfig = OBJECT_MAPPER.readValue(
                    configContent,
                    new TypeReference<Properties>() {
                    }
            );

            CACHE.clear();
            CACHE.putAll(newConfig);

            log.info("[AgentConfig] config refreshed, size={}, content={}",
                    CACHE.size(), newConfig);

        } catch (Exception e) {
            log.error("[AgentConfig] refresh cache failed, content={}", configContent, e);
        }
    }

    public static String get(String key) {
        return CACHE.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        return CACHE.getProperty(key, defaultValue);
    }

}
