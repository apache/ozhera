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
package org.apache.ozhera.log.stream.job.extension;

import org.apache.ozhera.log.model.LogtailConfig;
import org.apache.ozhera.log.model.SinkConfig;

import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/8/20 14:40
 */
public interface StreamCommonExtension {
    String dataPreProcess(String data);

    Boolean checkUniqueMarkExists(String uniqueMark, Map<String, Map<Long, String>> config);

    Map<Long, String> getConfigMapByUniqueMark(Map<String, Map<Long, String>> config, String uniqueMark);

    Boolean preCheckTaskExecution(SinkConfig sinkConfig, LogtailConfig logTailConfig, Long logSpaceId);
}
