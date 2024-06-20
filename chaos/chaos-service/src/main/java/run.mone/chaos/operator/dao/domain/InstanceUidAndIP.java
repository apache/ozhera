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

/**
 * @author zhangxiaowei6
 * @Date 2024/4/9 14:57
 */

@Data
public class InstanceUidAndIP {
    private String instanceUid;
    private String ip;
    private String containerId;

    /**
     * stress 执行结果的记忆参数
     */
    private String memoryInstance;

    private String memoryInstanceUid;

    private String cpuInstance;

    private String cpuInstanceUid;

    public InstanceUidAndIP(String instanceUid, String ip) {
        this.instanceUid = instanceUid;
        this.ip = ip;
    }

    public InstanceUidAndIP() {

    }
}
