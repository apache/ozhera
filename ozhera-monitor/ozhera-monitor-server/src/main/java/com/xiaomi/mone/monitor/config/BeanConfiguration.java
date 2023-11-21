package com.xiaomi.mone.monitor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class BeanConfiguration {

    @Bean("heraRequestMappingExecutor")
    public ThreadPoolExecutor heraRequestMappingExecutor() {
        return new ThreadPoolExecutor(1, 20, 5, TimeUnit.MINUTES, new LinkedBlockingQueue(20),
                (Runnable r) -> new Thread(r, "compute-execute-thread-v2"), new ThreadPoolExecutor.CallerRunsPolicy());
    }
}
