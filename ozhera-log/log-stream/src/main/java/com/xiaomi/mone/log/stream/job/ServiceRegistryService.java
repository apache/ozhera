package com.xiaomi.mone.log.stream.job;

import com.xiaomi.mone.log.common.Config;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import com.xiaomi.youpin.docean.plugin.nacos.NacosNaming;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import static com.xiaomi.mone.log.stream.common.util.StreamUtils.*;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/12/25 14:38
 */
@Service
@Slf4j
public class ServiceRegistryService {

    @Value("$app_name")
    private String serviceName;

    @Value("$nacos_config_server_addr")
    private String nacosAddr;

    @Resource
    private NacosNaming nacosNaming;

    public void init() {
        try {
            nacosNaming.registerInstance(serviceName, buildInstance(serviceName));
        } catch (Exception e) {
            log.error("register stream service error,nacos address:{}", nacosAddr, e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                nacosNaming.deregisterInstance(serviceName, getIp(), getServicePort());
            } catch (Exception e) {
                log.warn("Failed to deregister service instance during shutdown", e);
            }
        }));
    }

    private int getServicePort() {
        return Integer.parseInt(Config.ins().get("service_port", DEFAULT_SERVER_PORT));
    }
}
