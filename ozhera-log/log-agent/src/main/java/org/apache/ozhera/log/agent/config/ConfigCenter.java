package org.apache.ozhera.log.agent.config;

/**
 * @author wtt
 * @date 2025/12/23 14:59
 * @version 1.0
 */
public interface ConfigCenter {
    /**
     * get the configuration content (json/yaml/properties are all fine, the Agent does not care)
     */
    String getConfig(String dataId) throws Exception;

    /**
     * listen for configuration changes
     */
    void addListener(String dataId, ConfigChangeListener listener);
}
