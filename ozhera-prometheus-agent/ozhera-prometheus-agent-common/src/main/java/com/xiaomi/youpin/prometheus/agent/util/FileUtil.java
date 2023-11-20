package com.xiaomi.youpin.prometheus.agent.util;


import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class FileUtil {

    private static ReentrantLock lock = new ReentrantLock();

    @SneakyThrows
    public static String LoadFile(String path) {
        lock.lock();
        try {
            log.info("FileUtil LoadFile path: {}", path);
            File file = new File(path);
            if (!file.exists() || !isCanReadFile(file)) {
                //Unreadable, non-existent, then exit.
                return "";
            }
            String content = FileUtils.readFileToString(file, "UTF-8");
            return content;
        }finally {
            lock.unlock();
        }
    }

    @SneakyThrows
    public static String WriteFile(String path, String content) {
        lock.lock();
        try {
            log.info("FileUtil WriteFile path: {}", path);
            File file = new File(path);
            if (!file.exists() || !isCanWriteFile(file)) {
                //Does not exist, cannot be written, then exit.
                return "";
            }
            if (path.equals("/usr/local/etc/prometheus.yml")) {
                log.info("checkNullFile path: {},content: {}", path, content);
            }
            FileUtils.write(file, content);
            return "success";
        }finally {
            lock.unlock();
        }
    }

    //Check file existence
    @SneakyThrows
    public static boolean IsHaveFile(String path) {
        log.info("FileUtil IsHaveFile path: {}", path);
        File file = new File(path);
        return file.exists();
    }

    //Delete file.
    @SneakyThrows
    public static boolean DeleteFile(String path) {
        lock.lock();
        try {
            log.info("FileUtil DeleteFile path: {}", path);
            File file = new File(path);
            boolean delete = file.delete();
            return delete;
        }finally {
            lock.unlock();
        }
    }

    //Check the readability of the document.
    @SneakyThrows
    private static boolean isCanReadFile(File file) {
        log.info("FileUtil isCanReadFile file: {}", file.getAbsoluteFile());
        //The file is unreadable.
        return file.canRead();
    }

    //Check file writability
    @SneakyThrows
    private static boolean isCanWriteFile(File file) {
        log.info("FileUtil isCanWriteFile file: {}", file.getAbsoluteFile());
        //The file is not writable.
        return file.canWrite();
    }

    //Rename file
    @SneakyThrows
    public static boolean RenameFile(String oldPath, String newPath) {
        lock.lock();
        try {
            log.info("FileUtil RenameFile oldPath: {},newPath: {}", oldPath, newPath);
            File file = new File(oldPath);
            if (!file.exists()) {
                return false;
            }
            boolean res = file.renameTo(new File(newPath));
            return res;
        }finally {
            lock.unlock();
        }
    }

    @SneakyThrows
    public static void GenerateFile(String path) {
        lock.lock();
        try {
            log.info("FileUtil GenerateFile path: {}", path);
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
            }
        }finally {
            lock.unlock();
        }
    }

}
