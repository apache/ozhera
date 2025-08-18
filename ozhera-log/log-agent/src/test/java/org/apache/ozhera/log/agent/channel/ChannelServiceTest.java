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
package org.apache.ozhera.log.agent.channel;

import com.google.common.collect.Lists;
import com.xiaomi.mone.file.LogFile;
import org.apache.ozhera.log.utils.SimilarUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/2/16 11:10
 */
@Slf4j
public class ChannelServiceTest {

    @Test
    public void monitorTest() {
        String logPattern = "/home/work/logs/neo-logs/jump-game-stable-74bbdbcf9d-sf2vx/applogs/i18n-shop-jump-game/log_debug.log|log_error.log|log_info.log|log_warn.log|sys.log";
        String changedFilePath = "/home/work/logs/neo-logs/jump-game-stable-74bbdbcf9d-sf2vx/applogs/i18n-shop-jump-game/info/log-info.2023443";
        String logSplitExpress = "/home/work/logs/neo-logs/jump-game-stable-74bbdbcf9d-sf2vx/applogs/i18n-shop-jump-game/(debug/log-debug.*|error/log-error.*|info/log-info.*|warn/log-warn.*|sys.*)";
        List<String> patterns = Arrays.asList("/home/work/logs/neo-logs/jump-game-stable-74bbdbcf9d-sf2vx/applogs/i18n-shop-jump-game/log_debug.log",
                "/home/work/logs/neo-logs/jump-game-stable-74bbdbcf9d-sf2vx/applogs/i18n-shop-jump-game/log_error.log",
                "/home/work/logs/neo-logs/jump-game-stable-74bbdbcf9d-sf2vx/applogs/i18n-shop-jump-game/log_info.log",
                "/home/work/logs/neo-logs/jump-game-stable-74bbdbcf9d-sf2vx/applogs/i18n-shop-jump-game/log_warn.log",
                "/home/work/logs/neo-logs/jump-game-stable-74bbdbcf9d-sf2vx/applogs/i18n-shop-jump-game/sys.log");
        ConcurrentHashMap<String, LogFile> logFileMap = new ConcurrentHashMap<>();
        String channelId = "";
        String localIp = "";
        String separator = "/";
        String baseFileName = logPattern.substring(logPattern.lastIndexOf(separator) + 1);
        log.warn("#### watch consumer accept file:{},logPattern:{}", changedFilePath, logPattern);
        String changeFileName = StringUtils.substringAfterLast(changedFilePath, separator);
        Pattern pattern = makeLogPattern(logPattern, logSplitExpress);
        String originFileName = StringUtils.substringAfterLast(logPattern, separator);
        boolean ifTo = true;
        if (changeFileName.contains("wf")) {
            String changeFilePrefix = StringUtils.substringBeforeLast(changeFileName, "-");
            ifTo = changeFilePrefix.equals(originFileName);
        }
        if (pattern.matcher(changedFilePath).matches() && ifTo) {
            List<String> fileNames = Arrays.stream(baseFileName.split("\\|")).collect(Collectors.toList());
            String fileName = SimilarUtils.findHighestSimilarityStr(changeFileName, fileNames);
            String newFilePath = changedFilePath.substring(0, changedFilePath.lastIndexOf(separator)) + separator + fileName;
            if (StringUtils.isNotEmpty(logSplitExpress)) {
                String finalFileName = fileName;
                newFilePath = patterns.stream().filter(logPath -> logPath.contains(finalFileName)).findFirst().get();
            }
            log.warn("newFilePath:{}", newFilePath);
            LogFile logFile = logFileMap.get(newFilePath);
            if (null == logFile) {
//                readFile(input.getPatternCode(), localIp, newFilePath, channelId);
                log.info("watch new file create for chnnelId:{},ip:{},path:{}", channelId, localIp, newFilePath);
            } else {
                try {
                    TimeUnit.SECONDS.sleep(7);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                logFile.setReOpen(true);
                log.info("file reOpen: chnnelId:{},ip:{},path:{}", channelId, localIp, newFilePath);
            }
        } else {
            log.info("invalid file create event, logPattern:{}, changedFilePath:{}", logPattern, changedFilePath);
        }
    }

    public Pattern makeLogPattern(String logPattern, String logSplitExpress) {
        if (StringUtils.isNotEmpty(logSplitExpress)) {
            return Pattern.compile(logSplitExpress);
        }
        String separator = FileSystems.getDefault().getSeparator();
        List<String> pathList = Lists.newArrayList();
        for (String filePath : logPattern.split(",")) {
            String filePrefix = StringUtils.substringBeforeLast(filePath, separator);
            String multipleFileNames = StringUtils.substringAfterLast(filePath, separator);
            if (filePath.contains("*") && !filePath.contains(".*")) {
                logPattern = logPattern.replaceAll("\\*", ".*");
            } else {
                logPattern = Arrays.stream(multipleFileNames.split("\\|"))
                        .map(s -> filePrefix + separator + s + ".*")
                        .collect(Collectors.joining("|"));
            }
            if (!logPattern.endsWith(".*")) {
                logPattern = logPattern + ".*";
            }
            pathList.add(logPattern);
        }
        log.warn("logPattern -> regex:{}", logPattern);
        return Pattern.compile(logPattern);
    }


    @Test
    public void testShouldCollectLogs() {
        String line = "2025-02-12 19:24:27,076|WARN ||ExecutorUtil-STP-Virtual-Thread1|o.a.ozhera.log.agent.common.ExecutorUtil|65|Executor statistic STP_EXECUTOR:java.util.concurrent.ScheduledThreadPoolExecutor@792b749c[Running, pool size = 6, active threads = 1, queued tasks = 0, completed tasks = 5]";
        List list = new ArrayList();
        list.add("INFO");
        list.add("WARN");
        list.add("ERROR");
        System.out.println(shouldCollectLogs(list, line, 40));
    }

    @Test
    public void test() {
        String logContent =
                "19:52:35,792 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - debug attribute not set\n" +
                        "19:52:35,794 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - About to instantiate appender of type [ch.qos.logback.core.ConsoleAppender]\n" +
                        "19:52:35,796 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - Naming appender as [stdout]\n" +
                        "19:52:35,849 |-WARN in ch.qos.logback.core.ConsoleAppender[stdout] - This appender no longer admits a layout as a sub-component, set an encoder instead.\n" +
                        "19:52:35,849 |-WARN in ch.qos.logback.core.ConsoleAppender[stdout] - To ensure compatibility, wrapping your layout in LayoutWrappingEncoder.\n" +
                        "19:52:35,849 |-WARN in ch.qos.logback.core.ConsoleAppender[stdout] - See also http://logback.qos.ch/codes.html#layoutInsteadOfEncoder for details\n" +
                        "19:52:35,849 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - About to instantiate appender of type [ch.qos.logback.core.rolling.RollingFileAppender]\n" +
                        "19:52:35,851 |-INFO in ch.qos.logback.core.joran.action.AppenderAction - Naming appender as [logfile]\n" +
                        "19:52:35,851 |-INFO in ch.qos.logback.core.joran.action.NestedComplexPropertyIA - Assuming default type [ch.qos.logback.classic.encoder.PatternLayoutEncoder] for [encoder] property\n" +
                        "19:52:35,859 |-INFO in c.q.l.core.rolling.TimeBasedRollingPolicy@1794717576 - No compression will be used\n" +
                        "19:52:35,859 |-INFO in c.q.l.core.rolling.TimeBasedRollingPolicy@1794717576 - Will use the pattern /home/work/log/log-agent/server.log.%d{yyyy-MM-dd-HH} for the active file\n" +
                        "19:52:35,861 |-INFO in c.q.l.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy - The date pattern is 'yyyy-MM-dd-HH' from file name pattern '/home/work/log/log-agent/server.log.%d{yyyy-MM-dd-HH}'.\n" +
                        "19:52:35,861 |-INFO in c.q.l.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy - Roll-over at the top of every hour.\n" +
                        "19:52:35,904 |-INFO in c.q.l.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy - Setting initial period to Wed Feb 12 19:52:35 CST 2025\n" +
                        "19:52:35,905 |-INFO in c.q.l.core.rolling.TimeBasedRollingPolicy@1794717576 - Cleaning on start up\n" +
                        "19:52:35,907 |-INFO in c.q.l.core.rolling.helper.TimeBasedArchiveRemover - first clean up after appender initialization\n" +
                        "19:52:35,908 |-INFO in ch.qos.logback.core.rolling.RollingFileAppender[logfile] - Active log file name: /home/work/log/log-agent/server.log\n" +
                        "19:52:35,908 |-INFO in ch.qos.logback.core.rolling.RollingFileAppender[logfile] - File property is set to [/home/work/log/log-agent/server.log]\n" +
                        "19:52:36,177 |-ERROR in ch.qos.logback.core.rolling.RollingFileAppender[logfile] - Failed to create parent directories for [/home/work/log/log-agent/server.log]\n" +
                        "19:52:36,188 |-ERROR in ch.qos.logback.core.rolling.RollingFileAppender[logfile] - openFile(/home/work/log/log-agent/server.log,true) call failed. java.io.FileNotFoundException: /home/work/log/log-agent/server.log (No such file or directory)\n" +
                        "\tat java.io.FileNotFoundException: /home/work/log/log-agent/server.log (No such file or directory)\n" +
                        "\tat \tjava.base/java.io.FileOutputStream.open0(Native Method)\n" +
                        "\tat \tjava.base/java.io.FileOutputStream.open(FileOutputStream.java:289)\n" +
                        "\tat \tjava.base/java.io.FileOutputStream.<init>(FileOutputStream.java:230)\n" +
                        "\tat \tch.qos.logback.core.recovery.ResilientFileOutputStream.<init>(ResilientFileOutputStream.java:26)\n" +
                        "\tat \tch.qos.logback.core.FileAppender.openFile(FileAppender.java:204)\n" +
                        "\tat \tch.qos.logback.core.FileAppender.start(FileAppender.java:127)\n" +
                        "\tat \tch.qos.logback.core.rolling.RollingFileAppender.start(RollingFileAppender.java:100)\n" +
                        "\tat \tch.qos.logback.core.joran.action.AppenderAction.end(AppenderAction.java:90)\n" +
                        "\tat \tch.qos.logback.core.joran.spi.Interpreter.callEndAction(Interpreter.java:309)\n" +
                        "\tat \tch.qos.logback.core.joran.spi.Interpreter.endElement(Interpreter.java:193)\n" +
                        "\tat \tch.qos.logback.core.joran.spi.Interpreter.endElement(Interpreter.java:179)\n" +
                        "\tat \tch.qos.logback.core.joran.spi.EventPlayer.play(EventPlayer.java:62)\n" +
                        "\tat \tch.qos.logback.core.joran.GenericConfigurator.doConfigure(GenericConfigurator.java:165)\n" +
                        "\tat \tch.qos.logback.core.joran.GenericConfigurator.doConfigure(GenericConfigurator.java:152)\n" +
                        "\tat \tch.qos.logback.core.joran.GenericConfigurator.doConfigure(GenericConfigurator.java:110)\n" +
                        "\tat \tch.qos.logback.core.joran.GenericConfigurator.doConfigure(GenericConfigurator.java:53)\n" +
                        "\tat \tch.qos.logback.classic.util.ContextInitializer.configureByResource(ContextInitializer.java:64)\n" +
                        "\tat \tch.qos.logback.classic.util.ContextInitializer.autoConfig(ContextInitializer.java:134)\n" +
                        "\tat \torg.slf4j.impl.StaticLoggerBinder.init(StaticLoggerBinder.java:84)\n" +
                        "\tat \torg.slf4j.impl.StaticLoggerBinder.<clinit>(StaticLoggerBinder.java:55)\n" +
                        "\tat \torg.slf4j.LoggerFactory.bind(LoggerFactory.java:150)\n" +
                        "\tat \torg.slf4j.LoggerFactory.performInitialization(LoggerFactory.java:124)\n" +
                        "\tat \torg.slf4j.LoggerFactory.getILoggerFactory(LoggerFactory.java:417)\n" +
                        "\tat \torg.slf4j.LoggerFactory.getLogger(LoggerFactory.java:362)\n" +
                        "\tat \torg.slf4j.LoggerFactory.getLogger(LoggerFactory.java:388)\n" +
                        "\tat \torg.apache.ozhera.log.agent.channel.ChannelServiceTest.<clinit>(ChannelServiceTest.java:44)\n" +
                        "\tat \tjava.base/jdk.internal.misc.Unsafe.ensureClassInitialized0(Native Method)\n" +
                        "\tat \tjava.base/jdk.internal.misc.Unsafe.ensureClassInitialized(Unsafe.java:1160)\n" +
                        "\tat \tjava.base/jdk.internal.reflect.MethodHandleAccessorFactory.ensureClassInitialized(MethodHandleAccessorFactory.java:340)\n" +
                        "\tat \tjava.base/jdk.internal.reflect.MethodHandleAccessorFactory.newConstructorAccessor(MethodHandleAccessorFactory.java:103)\n" +
                        "\tat \tjava.base/jdk.internal.reflect.ReflectionFactory.newConstructorAccessor(ReflectionFactory.java:173)\n" +
                        "\tat \tjava.base/java.lang.reflect.Constructor.acquireConstructorAccessor(Constructor.java:549)\n" +
                        "\tat \tjava.base/java.lang.reflect.Constructor.newInstanceWithCaller(Constructor.java:499)\n" +
                        "\tat \tjava.base/java.lang.reflect.Constructor.newInstance(Constructor.java:486)\n" +
                        "\tat \torg.junit.runners.BlockJUnit4ClassRunner.createTest(BlockJUnit4ClassRunner.java:250)\n" +
                        "\tat \torg.junit.runners.BlockJUnit4ClassRunner.createTest(BlockJUnit4ClassRunner.java:260)\n" +
                        "\tat \torg.junit.runners.BlockJUnit4ClassRunner$2.runReflectiveCall(BlockJUnit4ClassRunner.java:309)\n" +
                        "\tat \torg.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)\n" +
                        "\tat \torg.junit.runners.BlockJUnit4ClassRunner.methodBlock(BlockJUnit4ClassRunner.java:306)\n" +
                        "\tat \torg.junit.runners.BlockJUnit4ClassRunner$1.evaluate(BlockJUnit4ClassRunner.java:100)\n" +
                        "\tat \torg.junit.runners.ParentRunner.runLeaf(ParentRunner.java:366)\n" +
                        "\tat \torg.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:103)\n" +
                        "\tat \torg.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:63)\n" +
                        "\tat \torg.junit.runners.ParentRunner$4.run(ParentRunner.java:331)\n" +
                        "\tat \torg.junit.runners.ParentRunner$1.schedule(ParentRunner.java:79)\n" +
                        "\tat \torg.junit.runners.ParentRunner.runChildren(ParentRunner.java:329)\n" +
                        "\tat \torg.junit.runners.ParentRunner.access$100(ParentRunner.java:66)\n" +
                        "\tat \torg.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:293)\n" +
                        "\tat \torg.junit.runners.ParentRunner$3.evaluate(ParentRunner.java:306)\n" +
                        "\tat \torg.junit.runners.ParentRunner.run(ParentRunner.java:413)\n" +
                        "\tat \torg.junit.runner.JUnitCore.run(JUnitCore.java:137)\n" +
                        "\tat \tcom.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:69)\n" +
                        "\tat \tcom.intellij.rt.junit.IdeaTestRunner$Repeater$1.execute(IdeaTestRunner.java:38)\n" +
                        "\tat \tcom.intellij.rt.execution.junit.TestsRepeater.repeat(TestsRepeater.java:11)\n" +
                        "\tat \tcom.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:35)\n" +
                        "\tat \tcom.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:232)\n" +
                        "\tat \tcom.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:55)\n" +
                        "19:52:36,188 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.springframework] to ERROR\n" +
                        "19:52:36,188 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [ch.qos.logback] to ERROR\n" +
                        "19:52:36,188 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [com.xiaomi.data.push.service.state] to ERROR\n" +
                        "19:52:36,188 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [org.reflections.Reflections] to ERROR\n" +
                        "19:52:36,188 |-INFO in ch.qos.logback.classic.joran.action.LoggerAction - Setting level of logger [com.xiaomi.infra.galaxy] to ERROR\n" +
                        "19:52:36,188 |-INFO in ch.qos.logback.classic.joran.action.RootLoggerAction - Setting level of ROOT logger to INFO\n" +
                        "19:52:36,188 |-INFO in ch.qos.logback.core.joran.action.AppenderRefAction - Attaching appender named [stdout] to Logger[ROOT]\n" +
                        "19:52:36,189 |-INFO in ch.qos.logback.core.joran.action.AppenderRefAction - Attaching appender named [logfile] to Logger[ROOT]\n" +
                        "19:52:36,189 |-INFO in ch.qos.logback.classic.joran.action.ConfigurationAction - End of configuration.\n" +
                        "19:52:36,189 |-INFO in ch.qos.logback.classic.joran.JoranConfigurator@6ef888f6 - Registering current configuration as safe fallback point";

        List<String> lines = new ArrayList<>(Arrays.asList(logContent.split("\n")));
        long s = System.currentTimeMillis();
        List list = new ArrayList();
        list.add("WARN");
        list.add("ERROR");
        System.out.println("++++++++++++");
        for (String line : lines) {
            if (shouldCollectLogs(list, line, 40)) {
                System.out.println(line);
            }
        }
        long e = System.currentTimeMillis();
        System.out.println(e - s);

    }


    public Boolean shouldCollectLogs(List<String> logLevelList, String line, Integer prefixLength) {
        if (logLevelList == null || logLevelList.isEmpty()) {
            return true;
        }
        if (line == null || line.isEmpty()) {
            return false;
        }
        if (line.length() > prefixLength) {
            line = line.substring(0, prefixLength);
        }
        for (String level : logLevelList) {
            if (line.contains(level)) {
                return true;
            }
        }
        return false;
    }
}
