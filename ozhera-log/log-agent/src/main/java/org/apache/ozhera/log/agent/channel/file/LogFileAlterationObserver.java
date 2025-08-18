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

import cn.hutool.core.util.ReflectUtil;
import com.xiaomi.data.push.rpc.common.RemotingUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.io.monitor.FileEntry;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Comparator;

import static org.apache.ozhera.log.agent.channel.file.InodeFileComparator.INODE_COMPARATOR;

/**
 * @author wtt
 * @version 1.0
 * @description Rewritten file listener, removes events for modifications and deletions,
 * only triggers events for new file additions.
 * @date 2023/7/14 10:51
 */
@Slf4j
public class LogFileAlterationObserver extends FileAlterationObserver {

    private static final FileEntry[] EMPTY_ENTRIES = new FileEntry[0];

    private FileEntry childRootEntry;
    private Comparator<File> childComparator;

    public LogFileAlterationObserver(String directoryName) {
        this(new File(directoryName));
    }

    public LogFileAlterationObserver(String directoryName, FileFilter fileFilter) {
        this(new File(directoryName), fileFilter);
    }

    public LogFileAlterationObserver(String directoryName, FileFilter fileFilter, IOCase caseSensitivity) {
        this(new File(directoryName), fileFilter, caseSensitivity);
    }

    public LogFileAlterationObserver(File directory) {
        this(directory, null);
    }

    public LogFileAlterationObserver(File directory, FileFilter fileFilter) {
        this(directory, fileFilter, null);
    }

    public LogFileAlterationObserver(File directory, FileFilter fileFilter, IOCase caseSensitivity) {
        this(new FileEntry(directory), fileFilter, caseSensitivity);
    }

    protected LogFileAlterationObserver(FileEntry rootEntry, FileFilter fileFilter, IOCase caseSensitivity) {
        super(rootEntry, fileFilter, caseSensitivity);
        if (!RemotingUtil.isWindowsPlatform()) {
            log.info("LogFileAlterationObserver set comparator:{}", INODE_COMPARATOR);
            ReflectUtil.setFieldValue(this, "comparator", INODE_COMPARATOR);
        }
    }

    @Override
    public void checkAndNotify() {
        try {
            /* fire onStart() */
            for (FileAlterationListener listener : getListeners()) {
                listener.onStart(this);
            }

            /* fire directory/file events */
            File rootFile = getDirectory();
            if (null == childRootEntry) {
                childRootEntry = (FileEntry) ReflectUtil.getFieldValue(this, "rootEntry");
            }
            if (rootFile.exists()) {
//                log.info("LogFileAlterationObserver checkAndNotify rootFile:{}", rootFile.getAbsolutePath());
                checkAndNotify(childRootEntry, childRootEntry.getChildren(), listFiles(rootFile));
            } else if (childRootEntry.isExists()) {
//                log.info("LogFileAlterationObserver checkAndNotify rootFile:{}", childRootEntry.getFile().getAbsolutePath());
                checkAndNotify(childRootEntry, childRootEntry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
            }

            /* fire onStop() */
            for (FileAlterationListener listener : getListeners()) {
                listener.onStop(this);
            }
        } catch (Exception e) {
            log.error("LogFileAlterationObserver checkAndNotify", e);
        }
    }

    private void checkAndNotify(FileEntry parent, FileEntry[] previous, File[] files) {
        int c = 0;
        if (null == childComparator) {
            childComparator = (Comparator) ReflectUtil.getFieldValue(this, "comparator");
        }
//        List<File> oldFileList = Arrays.stream(previous).map(FileEntry::getFile).collect(Collectors.toList());
//        List<String> newFileList = Arrays.stream(files).map(File::getAbsolutePath).collect(Collectors.toList());
//        log.info("childComparator:{},previous:{},files:{}", childComparator, GSON.toJson(oldFileList), GSON.toJson(newFileList));
        FileEntry[] current = files.length > 0 ? new FileEntry[files.length] : EMPTY_ENTRIES;
        for (FileEntry entry : previous) {
            while (c < files.length && childComparator.compare(entry.getFile(), files[c]) > 0) {
                current[c] = createFileEntry(parent, files[c]);
                doCreate(current[c]);
                c++;
            }
            if (c < files.length && childComparator.compare(entry.getFile(), files[c]) == 0) {
                doMatch(entry, files[c]);
                checkAndNotify(entry, entry.getChildren(), listFiles(files[c]));
                current[c] = entry;
                c++;
            } else {
                checkAndNotify(entry, entry.getChildren(), FileUtils.EMPTY_FILE_ARRAY);
            }
        }
        for (; c < files.length; c++) {
            current[c] = createFileEntry(parent, files[c]);
            doCreate(current[c]);
        }
        parent.setChildren(current);
    }

    private void doMatch(final FileEntry entry, final File file) {
        entry.refresh(file);
    }

    private File[] listFiles(File file) {
        File[] children = null;
        if (file.isDirectory()) {
            children = getFileFilter() == null ? file.listFiles() : file.listFiles(getFileFilter());
        }
        if (children == null) {
            children = FileUtils.EMPTY_FILE_ARRAY;
        }
        if (children.length > 1) {
            Arrays.sort(children, NameFileComparator.NAME_SYSTEM_COMPARATOR);
        }
        return children;
    }

    private FileEntry createFileEntry(FileEntry parent, File file) {
        FileEntry entry = parent.newChildInstance(file);
        entry.refresh(file);
        File[] files = listFiles(file);
        FileEntry[] children = files.length > 0 ? new FileEntry[files.length] : EMPTY_ENTRIES;
        for (int i = 0; i < files.length; i++) {
            children[i] = createFileEntry(entry, files[i]);
        }
        entry.setChildren(children);
        return entry;
    }

    private void doCreate(FileEntry entry) {
        for (FileAlterationListener listener : getListeners()) {
            if (entry.isDirectory()) {
                listener.onDirectoryCreate(entry.getFile());
            } else {
                listener.onFileCreate(entry.getFile());
            }
        }
        FileEntry[] children = entry.getChildren();
        for (FileEntry aChildren : children) {
            doCreate(aChildren);
        }
    }
}
