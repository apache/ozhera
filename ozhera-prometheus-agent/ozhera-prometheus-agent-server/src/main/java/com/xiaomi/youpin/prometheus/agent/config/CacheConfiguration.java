package com.xiaomi.youpin.prometheus.agent.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * @author zhangxiaowei6
 * @Date 2023/9/18 09:56
 */
@Configuration
@Slf4j
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class CacheConfiguration {
    @Bean
    public Cache<String, Object> guavaCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(100) // The maximum number of entries in the cache
                .expireAfterWrite(7200, TimeUnit.MINUTES) // The expiration time of the entry
                .build();
    }
}
