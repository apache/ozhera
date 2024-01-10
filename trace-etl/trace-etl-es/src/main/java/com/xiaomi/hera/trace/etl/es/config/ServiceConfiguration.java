package com.xiaomi.hera.trace.etl.es.config;

import com.xiaomi.hera.trace.etl.mapper.HeraTraceEtlConfigMapper;
import com.xiaomi.hera.trace.etl.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/4/28 10:32 am
 */
@Configuration
public class ServiceConfiguration {

    @Autowired
    private HeraTraceEtlConfigMapper heraTraceEtlConfigMapper;


    @Bean
    public ManagerService managerService(){
        return new ManagerService(heraTraceEtlConfigMapper);
    }

}
