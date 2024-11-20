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
package org.apache.ozhera.log.stream.job;

import org.apache.ozhera.log.model.StorageInfo;
import org.apache.ozhera.log.stream.common.SinkJobEnum;
import org.apache.ozhera.log.stream.sink.SinkChain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/8/22 16:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SinkJobConfig extends LogConfig {
    private String mqType;
    private String ak;
    private String sk;
    private String clusterInfo;
    private String topic;
    private String tag;
    private String index;
    private String keyList;
    private String valueList;
    private String parseScript;
    private String logStoreName;
    private SinkChain sinkChain;
    private String tail;
    private String storageType;
    private StorageInfo storageInfo;
    private List<String> columnList;
    private Integer parseType;
    /**
     * @see SinkJobEnum#name()
     */
    private String jobType;
    private String consumerGroup;
}
