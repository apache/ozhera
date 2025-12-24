package org.apache.ozhera.log.agent.config;

/**
 * @author wtt
 * @date 2025/12/23 14:59
 * @version 1.0
 */
@FunctionalInterface
public interface ConfigChangeListener {

    void onChange(String newConfig);

}
