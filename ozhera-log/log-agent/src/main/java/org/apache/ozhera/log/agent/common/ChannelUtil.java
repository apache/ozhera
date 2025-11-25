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
package org.apache.ozhera.log.agent.common;

import com.google.common.collect.Lists;
import com.xiaomi.youpin.docean.plugin.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.agent.channel.memory.ChannelMemory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.*;
import static org.apache.ozhera.log.common.PathUtils.PATH_WILDCARD;
import static org.apache.ozhera.log.common.PathUtils.SEPARATOR;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/6/23 14:55
 */
@Slf4j
public class ChannelUtil {

    private ChannelUtil() {

    }

    public static List<String> buildLogExpressList(String logPattern) {
        List<String> pathList = Lists.newArrayList();
        for (String filePath : logPattern.split(SYMBOL_COMMA)) {
            String filePrefix = StringUtils.substringBeforeLast(filePath, SEPARATOR);
            String multipleFileNames = StringUtils.substringAfterLast(filePath, SEPARATOR);
            if (filePath.contains(PATH_WILDCARD) && !filePath.contains(SYMBOL_MULTI)) {
                logPattern = logPattern.replaceAll("\\*", SYMBOL_MULTI);
            } else {
                logPattern = Arrays.stream(multipleFileNames.split("\\|"))
                        .map(s -> filePrefix + SEPARATOR + s + SYMBOL_MULTI)
                        .collect(Collectors.joining(DEFAULT_TAIL_SEPARATOR));
            }
            if (!logPattern.endsWith(SYMBOL_MULTI)) {
                logPattern = logPattern + SYMBOL_MULTI;
            }
            pathList.add(logPattern);
        }
        return pathList;
    }

    public static String buildSingleTimeExpress(String filePath) {
        String filePrefix = StringUtils.substringBeforeLast(filePath, SEPARATOR);
        String multipleFileName = StringUtils.substringAfterLast(filePath, SEPARATOR);
        if (!multipleFileName.contains(SYMBOL_MULTI)) {
            multipleFileName = multipleFileName.replaceAll("\\*", SYMBOL_MULTI);
        }
        if (multipleFileName.startsWith(PATH_WILDCARD)) {
            multipleFileName = multipleFileName.replaceFirst("\\*", SYMBOL_MULTI);
        }
        return String.format("%s%s%s", filePrefix, SEPARATOR, multipleFileName);
    }

    /**
     * The value can only be obtained from unix system files, otherwise it is an empty object.
     *
     * @param filePath
     * @return
     */
    public static ChannelMemory.UnixFileNode buildUnixFileNode(String filePath) {
        try {
            BasicFileAttributes fileAttributes = Files.readAttributes(Paths.get(filePath), BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
            if (null != fileAttributes.fileKey()) {
                ChannelMemory.UnixFileNode unixFileNode = GSON.fromJson(GSON.toJson(fileAttributes.fileKey()), ChannelMemory.UnixFileNode.class);
                log.debug("origin file path:{},fileNode unixFileNode:{}", filePath, GSON.toJson(unixFileNode));
                return unixFileNode;
            }
        } catch (Exception e) {
            log.error("buildUnixFileNode error,filePath:{}", filePath, e);
        }
        return new ChannelMemory.UnixFileNode();
    }

    /**
     * Get inode pair (memory inode and current inode) for a file
     *
     * @param channelMemory the channel memory containing file progress
     * @param filePath the file path to check
     * @return array with [memoryInode, currentInode] or null if not available
     */
    private static ChannelMemory.UnixFileNode[] getInodePair(ChannelMemory channelMemory, String filePath) {
        if (channelMemory == null || channelMemory.getFileProgressMap() == null) {
            return null;
        }

        ChannelMemory.FileProgress fileProgress = channelMemory.getFileProgressMap().get(filePath);
        if (fileProgress == null || fileProgress.getUnixFileNode() == null) {
            return null;
        }

        ChannelMemory.UnixFileNode memoryInode = fileProgress.getUnixFileNode();
        ChannelMemory.UnixFileNode currentInode = buildUnixFileNode(filePath);

        return new ChannelMemory.UnixFileNode[]{memoryInode, currentInode};
    }

    /**
     * Check if file inode has changed by comparing memory inode with current file inode
     *
     * @param channelMemory the channel memory containing file progress
     * @param filePath the file path to check
     * @return true if inode has changed, false otherwise
     */
    public static boolean isInodeChanged(ChannelMemory channelMemory, String filePath) {
        ChannelMemory.UnixFileNode[] inodePair = getInodePair(channelMemory, filePath);
        if (inodePair == null) {
            return false;
        }

        ChannelMemory.UnixFileNode memoryInode = inodePair[0];
        ChannelMemory.UnixFileNode currentInode = inodePair[1];

        return memoryInode.getSt_ino() != null && currentInode.getSt_ino() != null &&
                !java.util.Objects.equals(memoryInode.getSt_ino(), currentInode.getSt_ino());
    }

    /**
     * Get inode information for logging
     *
     * @param channelMemory the channel memory
     * @param filePath the file path
     * @return array with [oldInode, newInode] or null if not available
     */
    public static Long[] getInodeInfo(ChannelMemory channelMemory, String filePath) {
        ChannelMemory.UnixFileNode[] inodePair = getInodePair(channelMemory, filePath);
        if (inodePair == null) {
            return null;
        }

        ChannelMemory.UnixFileNode memoryInode = inodePair[0];
        ChannelMemory.UnixFileNode currentInode = inodePair[1];

        if (memoryInode.getSt_ino() != null && currentInode.getSt_ino() != null) {
            return new Long[]{memoryInode.getSt_ino(), currentInode.getSt_ino()};
        }
        return null;
    }

    public static long countFilesRecursive(File directory) {
        long count = 0;
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    count += countFilesRecursive(file);
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 获取配置,先从环境变量中获取,再从系统属性中获取,最后从配置文件中获取
     * @param key 配置的key
     * @param config 配置对象
     * @return 配置的值
     */
    public static String getConfig(String key, Config config) {
        String raw = System.getenv(key);
        if (StringUtils.isBlank(raw)) {
            raw = System.getProperty(key);
        }
        if (StringUtils.isBlank(raw)) {
            raw = config.get(key, "");
        }
        return raw;
    }

}
