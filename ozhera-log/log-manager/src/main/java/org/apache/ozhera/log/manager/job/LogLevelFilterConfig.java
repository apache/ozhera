package org.apache.ozhera.log.manager.job;

import org.apache.ozhera.log.manager.service.nacos.ManagerLogFilterConfig;

public interface LogLevelFilterConfig {
    ManagerLogFilterConfig getGlobalLogLevelFilterConfig();
}
