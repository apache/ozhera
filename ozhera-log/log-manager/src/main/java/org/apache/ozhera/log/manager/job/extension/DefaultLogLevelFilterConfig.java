package org.apache.ozhera.log.manager.job.extension;

import com.xiaomi.youpin.docean.anno.Component;
import org.apache.ozhera.log.manager.job.LogLevelFilterConfig;
import org.apache.ozhera.log.manager.service.nacos.ManagerLevelFilterConfigListener;
import org.apache.ozhera.log.manager.service.nacos.ManagerLogFilterConfig;

import javax.annotation.Resource;

@Component
public class DefaultLogLevelFilterConfig implements LogLevelFilterConfig {

    @Resource
    private ManagerLevelFilterConfigListener listener;

    @Override
    public ManagerLogFilterConfig getGlobalLogLevelFilterConfig() {
        return listener.queryFilterConfig();
    }
}
