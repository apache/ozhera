package org.apache.ozhera.log.agent.channel.listener;

import org.apache.ozhera.log.agent.channel.AbstractChannelService;
import org.apache.ozhera.log.agent.channel.ChannelDefine;
import org.apache.ozhera.log.agent.channel.memory.ChannelMemory;
import org.apache.ozhera.log.agent.channel.file.MonitorFile;
import org.apache.ozhera.log.agent.common.ChannelUtil;
import org.apache.ozhera.log.api.enums.LogTypeEnum;
import org.apache.ozhera.log.api.model.meta.FilterConf;
import org.apache.ozhera.log.agent.export.MsgExporter;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test for inode change detection when file is deleted and recreated with same name
 * This simulates the scenario where log rotation deletes and recreates files with same name
 */
public class InodeChangeDetectionTest {

    static class TestChannelService extends AbstractChannelService {
        volatile CopyOnWriteArrayList<String> canceled = new CopyOnWriteArrayList<>();
        volatile CopyOnWriteArrayList<String> reopened = new CopyOnWriteArrayList<>();
        List<MonitorFile> monitors;
        ChannelMemory channelMemory;
        String filePath;

        TestChannelService(List<MonitorFile> monitors, String filePath, ChannelMemory.UnixFileNode initialInode) {
            this.monitors = monitors;
            this.filePath = filePath;
            this.channelMemory = new ChannelMemory();
            Map<String, ChannelMemory.FileProgress> fileProgressMap = new HashMap<>();
            ChannelMemory.FileProgress fileProgress = new ChannelMemory.FileProgress();
            fileProgress.setUnixFileNode(initialInode);
            fileProgressMap.put(filePath, fileProgress);
            this.channelMemory.setFileProgressMap(fileProgressMap);
        }

        @Override public void start() {}
        @Override public void close() {}
        @Override public void deleteCollFile(String directory) {}
        @Override public void cancelFile(String file) { canceled.add(file); }
        @Override public void reOpen(String filePath) { reopened.add(filePath); }
        @Override public void cleanCollectFiles() {}
        @Override public void filterRefresh(List<FilterConf> confs) {}
        @Override public void refresh(ChannelDefine channelDefine, MsgExporter msgExporter) {}
        @Override public void stopFile(List<String> filePrefixList) {}
        @Override public org.apache.ozhera.log.agent.channel.ChannelState state() { return null; }
        @Override public List<MonitorFile> getMonitorPathList() { return monitors; }
        @Override public ChannelDefine getChannelDefine() { ChannelDefine d = new ChannelDefine(); d.setChannelId(1L); return d; }
        @Override public String instanceId() { return "test-inode"; }
        @Override public ChannelMemory getChannelMemory() { return channelMemory; }
        @Override public Map<String, Long> getExpireFileMap() { return new HashMap<>(); }
        @Override public Long getLogCounts() { return 0L; }
    }

    @Test
    public void ordinaryFileChanged_shouldCallReOpen() throws Exception {
        Path temp = Files.createTempFile("agent-inode", ".log");
        String realPath = temp.toString();
        
        TestChannelService service = new TestChannelService(
                List.of(MonitorFile.of(realPath, realPath + ".*", LogTypeEnum.APP_LOG_SIGNAL, false)),
                realPath, ChannelUtil.buildUnixFileNode(realPath));
        DefaultFileMonitorListener listener = new DefaultFileMonitorListener();
        listener.addChannelService(service);
        
        // Trigger file change event (ordinaryFileChanged now just calls reOpen)
        Method m = DefaultFileMonitorListener.class.getDeclaredMethod("ordinaryFileChanged", String.class);
        m.setAccessible(true);
        m.invoke(listener, realPath);
        
        // Verify reOpen was called (inode change detection is now handled in reOpen() method)
        Assert.assertTrue("File should be reopened",
                service.reopened.stream().anyMatch(p -> p.equals(realPath) || p.contains(realPath)));
        
        Files.deleteIfExists(temp);
    }

    @Test
    public void channelUtil_shouldDetectInodeChange() throws Exception {
        Path temp = Files.createTempFile("agent-inode-util", ".log");
        String realPath = temp.toString();
        ChannelMemory.UnixFileNode initialInode = ChannelUtil.buildUnixFileNode(realPath);
        
        if (initialInode.getSt_ino() == null) {
            Files.deleteIfExists(temp);
            return;
        }
        
        ChannelMemory channelMemory = new ChannelMemory();
        Map<String, ChannelMemory.FileProgress> fileProgressMap = new HashMap<>();
        ChannelMemory.FileProgress fileProgress = new ChannelMemory.FileProgress();
        fileProgress.setUnixFileNode(initialInode);
        fileProgressMap.put(realPath, fileProgress);
        channelMemory.setFileProgressMap(fileProgressMap);
        
        Assert.assertFalse("Inode should not be changed initially",
                ChannelUtil.isInodeChanged(channelMemory, realPath));
        
        Files.deleteIfExists(temp);
        Files.createFile(temp);
        
        Assert.assertTrue("Inode should be changed after file recreation",
                ChannelUtil.isInodeChanged(channelMemory, realPath));
        
        Files.deleteIfExists(temp);
    }
}
