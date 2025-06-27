package org.apache.ozhera.log.agent.extension.nacos;

import com.alibaba.nacos.api.config.ConfigFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import static org.apache.ozhera.log.common.Constant.DEFAULT_GROUP_ID;
import static org.apache.ozhera.log.common.Constant.DEFAULT_TIME_OUT_MS;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2025/6/10 16:52
 */
public class NacosConfigUtil {
    private final ConfigService configService;

    public NacosConfigUtil(String nacosAddr) throws NacosException {
        this.configService = ConfigFactory.createConfigService(nacosAddr);
    }

    public String getConfig(String dataId) throws NacosException {
        return configService.getConfig(dataId, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
    }
}
