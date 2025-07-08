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
package org.apache.ozhera.metrics.config;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version 1.0
 * @description SDK Logger Initializer for metrics logging configuration
 * @date 2025/5/12 16:01
 */
public class SdkLoggerInitializer {
    private static final Logger SDK_LOGGER;

    static {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // Configure encoder settings
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d|%-4.5level|%thread|%logger|%L|%msg%n");
        encoder.start();

        // Configure file appender
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setName("SDK_ROLLING_FILE_APPENDER");
        rollingFileAppender.setFile(LogFileNameUtil.getLogPathFile());
        rollingFileAppender.setEncoder(encoder);

        // Configure rolling policy
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern(LogFileNameUtil.getLogPathFile() + ".%i");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(10);
        rollingPolicy.start();

        // Configure size-based triggering policy
        SizeBasedTriggeringPolicy<ILoggingEvent> sizePolicy = new SizeBasedTriggeringPolicy<>();
        sizePolicy.setContext(loggerContext);
        sizePolicy.setMaxFileSize(FileSize.valueOf("10MB"));
        sizePolicy.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setTriggeringPolicy(sizePolicy);
        rollingFileAppender.start();

        // Configure async appender wrapper
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(loggerContext);
        asyncAppender.setName("ASYNC_SDK_APPENDER");
        asyncAppender.addAppender(rollingFileAppender);
        // Log buffer queue size
        asyncAppender.setQueueSize(5000);
        // When queue is full, discard logs below INFO level
        asyncAppender.setDiscardingThreshold(0);
        // Whether to record caller data (can reduce overhead)
        asyncAppender.setIncludeCallerData(false);
        asyncAppender.start();

        // Bind to specified logger
        ch.qos.logback.classic.Logger logbackLogger =
                loggerContext.getLogger("org.apache.ozhera.metrics.api.Metrics");
        logbackLogger.addAppender(asyncAppender);
        logbackLogger.setAdditive(false);

        SDK_LOGGER = logbackLogger;
    }

    // Provide org.slf4j.Logger type externally
    public static Logger getLogger() {
        return SDK_LOGGER;
    }
}
