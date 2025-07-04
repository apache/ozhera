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
package org.apache.ozhera.monitor.service.bizmetrics;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务指标自定义配置，通过nacos配置多个指标id和grafana dashboard url
 * <p>
 * 配置格式示例（JSON）：
 * {
 * "1001": {
 * "url": "https://grafana.example.com/d/abc123/dashboard1",
 * "type": "scene"
 * },
 * "1002": {
 * "url": "https://grafana.example.com/d/def456/dashboard2",
 * "type": "indicator"
 * }
 * }
 * <p>
 * 配置方式：
 * 1. 在Nacos中配置 biz.custom.metrics.config 项
 * 2. 值为JSON格式的指标ID到配置信息映射
 * 3. type字段支持："scene"、"indicator"
 *
 * @date 2025/06/17
 */
@Slf4j
@Component
public class BizCustomMetricsConfig {

    private static final String SCENE_TYPE_KEY = "scene";
    private static final String INDICATOR_TYPE_KEY = "indicator";

    /**
     * 配置项信息
     */
    public static class ConfigItem {
        private String url;
        private String type;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "ConfigItem{url='" + url + "', type=" + type + "}";
        }
    }

    /**
     * 业务指标配置JSON字符串
     */
    @NacosValue(value = "${biz.custom.metrics.config:{}}", autoRefreshed = true)
    private String configJson;

    /**
     * 配置是否启用（总开关）
     */
    @NacosValue(value = "${biz.custom.metrics.enabled:true}", autoRefreshed = true)
    private boolean enabled;

    private final Gson gson = new Gson();
    private final Type configType = new TypeToken<Map<String, ConfigItem>>() {
    }.getType();
    /**
     * 配置缓存 - 指标ID -> 配置项
     */
    private volatile Map<Long, ConfigItem> configCache;

    /**
     * 构造函数，确保configCache初始化
     */
    public BizCustomMetricsConfig() {
        this.configCache = new ConcurrentHashMap<>();
    }

    @PostConstruct
    private void init() {
        log.info("初始化业务指标自定义配置...");
        // 确保configCache已初始化
        if (configCache == null) {
            configCache = new ConcurrentHashMap<>();
        }
        refreshConfig();
        log.info("业务指标自定义配置初始化完成，当前配置数量: {}", configCache.size());
    }

    /**
     * 刷新配置（Nacos自动刷新时会调用）
     * 注意：当@NacosValue标注的字段值发生变化时，此方法会被自动调用
     */
    private void refreshConfig() {
        if (!enabled) {
            log.debug("业务指标自定义配置已禁用，跳过配置刷新");
            return;
        }

        try {
            // 确保configCache已初始化
            if (configCache == null) {
                configCache = new ConcurrentHashMap<>();
            }

            Map<String, ConfigItem> newConfigs = parseConfig(configJson);

            // 清空旧缓存
            configCache.clear();

            // 加载新配置
            if (newConfigs != null && !newConfigs.isEmpty()) {
                newConfigs.forEach((key, configItem) -> {
                    try {
                        Long indicatorId = Long.parseLong(key);
                        if (configItem != null && StringUtils.isNotBlank(configItem.getUrl())) {
                            configCache.put(indicatorId, configItem);
                            log.debug("加载指标配置: indicatorId={}, url={}, type={}",
                                    indicatorId, configItem.getUrl(), configItem.getType());
                        }
                    } catch (NumberFormatException e) {
                        log.warn("指标ID格式错误，跳过配置: key={}", key, e);
                    }
                });
            }

            log.info("业务指标配置刷新完成，有效配置数量: {}, 场景数量: {}, 指标数量: {}",
                    configCache.size(),
                    getConfigCount(SCENE_TYPE_KEY),
                    getConfigCount(INDICATOR_TYPE_KEY));

        } catch (Exception e) {
            log.error("刷新业务指标配置失败", e);
        }
    }

    /**
     * 解析配置JSON字符串
     */
    private Map<String, ConfigItem> parseConfig(String json) {
        if (StringUtils.isBlank(json) || "{}".equals(json.trim())) {
            return new HashMap<>();
        }

        try {
            Map<String, ConfigItem> result = gson.fromJson(json, configType);
            if (result != null) {
                log.debug("配置解析成功，配置项数量: {}", result.size());
                return result;
            }
        } catch (Exception e) {
            log.error("解析指标配置JSON失败: json={}", json, e);
        }

        return new HashMap<>();
    }

    /**
     * 根据场景ID获取Grafana dashboard URL（仅限场景类型）
     *
     * @param sceneId 场景ID
     * @return dashboard URL，如果未配置或不是场景类型则返回null
     */
    public String getSceneDashboardUrl(Long sceneId) {
        ConfigItem configItem = getConfigItem(sceneId);
        if (configItem != null && SCENE_TYPE_KEY.equals(configItem.getType())) {
            return configItem.getUrl();
        }
        return null;
    }

    /**
     * 根据指标ID获取Grafana dashboard URL（仅限指标类型）
     *
     * @param indicatorId 指标ID
     * @return dashboard URL，如果未配置或不是指标类型则返回null
     */
    public String getIndicatorDashboardUrl(Long indicatorId) {
        ConfigItem configItem = getConfigItem(indicatorId);
        if (configItem != null && INDICATOR_TYPE_KEY.equals(configItem.getType())) {
            return configItem.getUrl();
        }
        return null;
    }

    /**
     * 根据指标ID获取配置项
     *
     * @param indicatorId 指标ID
     * @return 配置项，如果未配置则返回null
     */
    public ConfigItem getConfigItem(Long indicatorId) {
        if (!enabled || indicatorId == null) {
            return null;
        }

        // 确保configCache已初始化
        if (configCache == null) {
            configCache = new ConcurrentHashMap<>();
            return null;
        }

        return configCache.get(indicatorId);
    }

    /**
     * 根据指标ID获取配置类型
     *
     * @param indicatorId 指标ID
     * @return 配置类型，如果未配置则返回null
     */
    public String getConfigType(Long indicatorId) {
        ConfigItem configItem = getConfigItem(indicatorId);
        return configItem != null ? configItem.getType() : null;
    }

    /**
     * 检查是否有场景类型的自定义配置
     *
     * @param sceneId 场景ID
     * @return true如果有场景类型的配置，否则false
     */
    public boolean hasSceneConfig(Long sceneId) {
        return isSceneType(sceneId);
    }

    /**
     * 检查是否有指标类型的自定义配置
     *
     * @param indicatorId 指标ID
     * @return true如果有指标类型的配置，否则false
     */
    public boolean hasIndicatorConfig(Long indicatorId) {
        return isIndicatorType(indicatorId);
    }

    /**
     * 检查指标是否为场景类型
     *
     * @param indicatorId 指标ID
     * @return true如果是场景类型，否则false
     */
    public boolean isSceneType(Long indicatorId) {
        String type = getConfigType(indicatorId);
        if (type == null) {
            return false;
        }
        return SCENE_TYPE_KEY.equals(type.trim());
    }

    /**
     * 检查指标是否为指标类型
     *
     * @param indicatorId 指标ID
     * @return true如果是指标类型，否则false
     */
    public boolean isIndicatorType(Long indicatorId) {
        String type = getConfigType(indicatorId);
        if (type == null) {
            return false;
        }
        return INDICATOR_TYPE_KEY.equals(type.trim());
    }

    /**
     * 获取所有配置
     *
     * @return 指标ID -> 配置项 映射
     */
    public Map<Long, ConfigItem> getAllConfigItems() {
        if (!enabled) {
            return new HashMap<>();
        }

        // 确保configCache已初始化
        if (configCache == null) {
            configCache = new ConcurrentHashMap<>();
            return new HashMap<>();
        }

        return new HashMap<>(configCache);
    }

    /**
     * 根据类型获取配置
     *
     * @param type 配置类型
     * @return 指定类型的配置映射
     */
    public Map<Long, ConfigItem> getConfigsByType(String type) {
        if (!enabled || type == null) {
            return new HashMap<>();
        }

        Map<Long, ConfigItem> result = new HashMap<>();
        getAllConfigItems().forEach((id, item) -> {
            if (type.equals(item.getType())) {
                result.put(id, item);
            }
        });
        return result;
    }

    /**
     * 获取场景类型的配置
     *
     * @return 场景类型的配置映射
     */
    public Map<Long, ConfigItem> getSceneConfigs() {
        return getConfigsByType(SCENE_TYPE_KEY);
    }

    /**
     * 获取指标类型的配置
     *
     * @return 指标类型的配置映射
     */
    public Map<Long, ConfigItem> getIndicatorConfigs() {
        return getConfigsByType(INDICATOR_TYPE_KEY);
    }

    /**
     * 获取配置数量
     *
     * @return 配置数量
     */
    public int getConfigCount() {
        if (!enabled || configCache == null) {
            return 0;
        }
        return configCache.size();
    }

    /**
     * 获取指定类型的配置数量
     *
     * @param type 配置类型
     * @return 指定类型的配置数量
     */
    public int getConfigCount(String type) {
        return getConfigsByType(type).size();
    }

}
