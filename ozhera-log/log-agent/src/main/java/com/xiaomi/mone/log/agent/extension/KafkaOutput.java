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
package com.xiaomi.mone.log.agent.extension;

import com.xiaomi.mone.log.agent.output.Output;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;


@Data
@EqualsAndHashCode
public class KafkaOutput extends Output implements Serializable {

    public static final String OUTPUT_KAFKAMQ = "kafka";

    private String serviceName = "KafkaService";

    /**
     * mq fill：namesrv_addr
     */
    private String clusterInfo;

    private String producerGroup;

    private String orgId;

    private String ak;

    private String sk;

    private String topic;

    private Integer partitionCnt;

    private Integer batchExportSize;

    @Override
    public String getEndpoint() {
        return clusterInfo;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }
}
