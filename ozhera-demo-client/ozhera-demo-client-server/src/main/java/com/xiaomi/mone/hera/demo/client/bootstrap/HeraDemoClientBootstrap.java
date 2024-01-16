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
package com.xiaomi.mone.hera.demo.client.bootstrap;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.spring.context.annotation.DubboComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author dingtao
 */
@EnableAutoConfiguration(exclude= {DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = {"com.xiaomi.mone.hera.demo.client"})
@DubboComponentScan(basePackages = "com.xiaomi.mone.hera.demo.client")
@Slf4j
public class HeraDemoClientBootstrap {

    public static void main(String... args) {
        try {
            SpringApplication.run(HeraDemoClientBootstrap.class, args);
        } catch (Throwable throwable) {
            log.error(throwable.getMessage(), throwable);
            System.exit(-1);
        }
    }
}