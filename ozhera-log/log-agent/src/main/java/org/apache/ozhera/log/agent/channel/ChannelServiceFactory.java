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

import cn.hutool.core.io.FileUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.agent.channel.memory.AgentMemoryService;
import org.apache.ozhera.log.agent.export.MsgExporter;
import org.apache.ozhera.log.agent.filter.FilterChain;
import org.apache.ozhera.log.agent.input.Input;
import org.apache.ozhera.log.api.enums.LogTypeEnum;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.apache.ozhera.log.common.Constant.SYMBOL_COMMA;
import static org.apache.ozhera.log.common.PathUtils.PATH_WILDCARD;
import static org.apache.ozhera.log.common.PathUtils.SEPARATOR;
import static org.apache.ozhera.log.utils.ConfigUtils.getConfigValue;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2025/3/13 10:00
 */
public class ChannelServiceFactory {

    private static final String DEFAULT_SPECIAL_FILE_SUFFIX_KEY = "special.file.suffix";

    private final AgentMemoryService agentMemoryService;
    private final String memoryBasePath;
    private static final Pattern regexCharsPattern = Pattern.compile("[*+?^${}()\\[\\]\\\\]");

    private List<String> multiSpecialFileSuffix;

    public ChannelServiceFactory(AgentMemoryService agentMemoryService, String memoryBasePath) {
        this.agentMemoryService = agentMemoryService;
        this.memoryBasePath = memoryBasePath;
        String specialFileSuffix = getConfigValue(DEFAULT_SPECIAL_FILE_SUFFIX_KEY);
        if (StringUtils.isNotBlank(specialFileSuffix)) {
            multiSpecialFileSuffix = Lists.newArrayList(specialFileSuffix.split(SYMBOL_COMMA));
        }
    }

    public ChannelService createChannelService(ChannelDefine channelDefine,
                                               MsgExporter exporter, FilterChain filterChain) {
        if (channelDefine == null || channelDefine.getInput() == null) {
            throw new IllegalArgumentException("Channel define or input cannot be null");
        }

        Input input = channelDefine.getInput();
        String logType = input.getType();
        String logPattern = input.getLogPattern();

        if (CollectionUtils.isNotEmpty(multiSpecialFileSuffix) && multiSpecialFileSuffix.stream().anyMatch(logPattern::endsWith)) {
            return createStandardChannelService(exporter, channelDefine, filterChain);
        }

        if (LogTypeEnum.OPENTELEMETRY == LogTypeEnum.name2enum(logType) || FileUtil.exist(logPattern)) {
            return createStandardChannelService(exporter, channelDefine, filterChain);
        }

        if (shouldUseWildcardService(logPattern)) {
            return createWildcardChannelService(exporter, channelDefine, filterChain);
        }

        return createStandardChannelService(exporter, channelDefine, filterChain);
    }

    private boolean shouldUseWildcardService(String logPattern) {
        if (StringUtils.isEmpty(logPattern)) {
            return false;
        }
        return isRegexPattern(logPattern) ||
                containsWildcard(logPattern);
    }

    private boolean containsWildcard(String logPattern) {
        return Arrays.stream(logPattern.split(SYMBOL_COMMA))
                .map(String::trim)
                .filter(StringUtils::isNotEmpty)
                .anyMatch(this::hasWildcardInPath);
    }

    private boolean hasWildcardInPath(String path) {
        String fileName = StringUtils.substringAfterLast(path, SEPARATOR);
        return !StringUtils.isEmpty(fileName) && fileName.contains(PATH_WILDCARD);
    }

    private boolean isRegexPattern(String logPattern) {
        if (StringUtils.isEmpty(logPattern)) {
            return false;
        }
        return regexCharsPattern.matcher(logPattern).find();
    }

    private ChannelService createStandardChannelService(MsgExporter exporter,
                                                        ChannelDefine channelDefine, FilterChain filterChain) {
        return new ChannelServiceImpl(exporter, agentMemoryService,
                channelDefine, filterChain);
    }

    private ChannelService createWildcardChannelService(MsgExporter exporter,
                                                        ChannelDefine channelDefine, FilterChain filterChain) {
        return new WildcardChannelServiceImpl(exporter, agentMemoryService,
                channelDefine, filterChain, memoryBasePath);
    }
}
