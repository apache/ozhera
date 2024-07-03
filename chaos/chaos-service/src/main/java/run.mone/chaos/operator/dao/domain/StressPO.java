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
package run.mone.chaos.operator.dao.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class StressPO implements Serializable {

    private Integer cpuNum;

    /**
     * load CPU with P percent loading for the CPU stress workers. 0 is effectively a sleep (no load) and 100 is full loading.
     */
    private Integer cpuLoad;

    private Integer vmNum;

    /**
     * mmap N bytes per vm worker, the default is 256MB.
     * One can specify the size as % of total available memory or in units of Bytes, KBytes, MBytes and GBytes using the suffix b, k, m or g.
     */
    private Integer vmBytes;

    private String vmUnit;

    /**
     * sleep N seconds before unmapping memory, the default is zero seconds. Specifying 0 will do an infinite wait.
     */
    private Integer vmHang;

    /**
     * 指定产生N个处理sync()函数的磁盘I/O进程,sync()用于将内存上的内容写到硬盘上
     */
    private Integer ioNum;

    /**
     * start N workers continually writing, reading and removing temporary files.
     * The default mode is to stress test sequential writes and reads.
     * With the --aggressive option enabled without any --hdd-opts options the hdd stressor will work through all the --hdd-opt options one by one to cover a range of I/O options.
     */
    private Integer hdd;

    /**
     * write N bytes for each hdd process, the default is 1 GB.
     * One can specify the size as % of free space on the file system or in units of Bytes, KBytes, MBytes and GBytes using the suffix b, k, m or g.
     */
    private Integer hddBytes;

    private String hddUnit;

    /**
     * stop stress test after T seconds. One can also specify the units of time in seconds, minutes, hours, days or years with the suffix s, m, h, d or y.
     * Note: A timeout of 0 will run stress-ng without any timeouts (run forever)
     */
    private Integer cpuTimeOut;

    private String cpuTimeOutUnit;

    private Integer vmTimeOut;

    private String vmTimeOutUnit;

    private String memoryInstance;

    private String memoryInstanceUid;

    private String cpuInstance;

    private String cpuInstanceUid;

    private List<InstanceUidAndIP> instanceUidAndIPList;

}
