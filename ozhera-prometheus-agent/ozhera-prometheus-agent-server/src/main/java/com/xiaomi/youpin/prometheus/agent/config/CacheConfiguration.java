/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
                .expireAfterWrite(7200, TimeUnit.SECONDS) // The expiration time of the entry
                .build();
    }
}
