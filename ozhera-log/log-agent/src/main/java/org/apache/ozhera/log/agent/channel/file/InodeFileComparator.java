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
package org.apache.ozhera.log.agent.channel.file;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.comparator.DefaultFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.ozhera.log.agent.channel.memory.ChannelMemory;
import org.apache.ozhera.log.agent.common.ChannelUtil;

import java.io.File;
import java.util.*;

/**
 * @author wtt
 * @version 1.0
 * @description File comparator, when the file names of two files are the same,
 * continue to compare whether the file inodes are the same. If they are not the same,
 * it means that the file has changed.
 * @date 2023/7/14 11:19
 */
@Slf4j
public class InodeFileComparator extends DefaultFileComparator {

    public static final Comparator<File> INODE_COMPARATOR = new InodeFileComparator();

    private static final Comparator<File> fileComparator = NameFileComparator.NAME_SYSTEM_COMPARATOR;

    private static final Map<String, Long> INODE_MAP = new HashMap<>();

    private static final List<String> filePaths = Lists.newArrayList();

    @Override
    public int compare(File file1, File file2) {
//        log.info("InodeFileComparator compare file1:{},file2:{},filePaths:{}", file1, file2, GSON.toJson(filePaths));
        int sort = fileComparator.compare(file1, file2);
        try {
            if (file1.isDirectory() || file2.isDirectory()) {
                return sort;
            }
            if (sort == 0 && filePaths.contains(file1.getAbsolutePath())) {
                //The file name is the same
//                log.info("INODE_MAP:{}", GSON.toJson(INODE_MAP));
                Long oldInode;
                if (INODE_MAP.containsKey(file1.getAbsolutePath())) {
                    oldInode = INODE_MAP.get(file1.getAbsolutePath());
                } else {
                    oldInode = ChannelUtil.buildUnixFileNode(file1.getAbsolutePath()).getSt_ino();
                    INODE_MAP.put(file1.getAbsolutePath(), oldInode);
                }
                ChannelMemory.UnixFileNode unixFileNode2 = ChannelUtil.buildUnixFileNode(file2.getAbsolutePath());
                if (!Objects.equals(oldInode, unixFileNode2.getSt_ino())) {
                    INODE_MAP.put(file2.getAbsolutePath(), unixFileNode2.getSt_ino());
                    return 1;
                }
            }
        } catch (Exception e) {
            log.error("InodeFileComparator compare error,file1:{},file2:{}", file1, file2, e);
        }
        return sort;
    }

    public static void addFile(String filePath) {
        log.info("InodeFileComparator add file : {}", filePath);
        filePaths.add(filePath);
    }

    public static void removeFile(String filePath) {
        log.info("InodeFileComparator remove file : {}", filePath);
        filePaths.remove(filePath);
    }
}
