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
 * @description
 * @date 2025/5/12 16:01
 */
public class SdkLoggerInitializer {
    private static final Logger SDK_LOGGER;

    static {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        // 编码器设置
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern("%d|%-4.5level|%thread|%logger|%L|%msg%n");
        encoder.start();

        // 文件 appender
        RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<>();
        rollingFileAppender.setContext(loggerContext);
        rollingFileAppender.setName("SDK_ROLLING_FILE_APPENDER");
        rollingFileAppender.setFile(LogFileNameUtil.getLogPathFile());
        rollingFileAppender.setEncoder(encoder);

        // 滚动策略
        FixedWindowRollingPolicy rollingPolicy = new FixedWindowRollingPolicy();
        rollingPolicy.setContext(loggerContext);
        rollingPolicy.setParent(rollingFileAppender);
        rollingPolicy.setFileNamePattern(LogFileNameUtil.getLogPathFile() + ".%i");
        rollingPolicy.setMinIndex(1);
        rollingPolicy.setMaxIndex(10);
        rollingPolicy.start();

        // 大小触发策略
        SizeBasedTriggeringPolicy<ILoggingEvent> sizePolicy = new SizeBasedTriggeringPolicy<>();
        sizePolicy.setContext(loggerContext);
        sizePolicy.setMaxFileSize(FileSize.valueOf("10MB"));
        sizePolicy.start();

        rollingFileAppender.setRollingPolicy(rollingPolicy);
        rollingFileAppender.setTriggeringPolicy(sizePolicy);
        rollingFileAppender.start();

        // 异步 appender 包装
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setContext(loggerContext);
        asyncAppender.setName("ASYNC_SDK_APPENDER");
        asyncAppender.addAppender(rollingFileAppender);
        // 日志缓冲队列大小
        asyncAppender.setQueueSize(5000);
        // 当队列满时，丢弃低于 INFO 级别的日志
        asyncAppender.setDiscardingThreshold(0);
        // 是否记录调用链数据（可减少开销）
        asyncAppender.setIncludeCallerData(false);
        asyncAppender.start();

        // 绑定到指定 logger
        ch.qos.logback.classic.Logger logbackLogger =
                loggerContext.getLogger("org.apache.ozhera.metrics.api.Metrics");
        logbackLogger.addAppender(asyncAppender);
        logbackLogger.setAdditive(false);

        SDK_LOGGER = logbackLogger;
    }

    // 对外提供 org.slf4j.Logger 类型
    public static Logger getLogger() {
        return SDK_LOGGER;
    }
}
