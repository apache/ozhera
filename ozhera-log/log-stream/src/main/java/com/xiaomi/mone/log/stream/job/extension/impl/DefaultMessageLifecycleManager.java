/*
 * Copyright (C) 2020 Xiaomi Corporation
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
package com.xiaomi.mone.log.stream.job.extension.impl;

import com.xiaomi.mone.log.api.model.msg.LineMessage;
import com.xiaomi.mone.log.stream.job.SinkJobConfig;
import com.xiaomi.mone.log.stream.job.extension.MessageLifecycleManager;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.xiaomi.mone.log.stream.common.LogStreamConstants.DEFAULT_MESSAGE_LIFECYCLE_MANAGER;

/**
 *
 * @description
 * @version 1.0
 * @author wtt
 * @date 2024/6/4 11:24
 *
 */
@Service(name = DEFAULT_MESSAGE_LIFECYCLE_MANAGER)
@Slf4j
public class DefaultMessageLifecycleManager implements MessageLifecycleManager {
    @Override
    public void beforeProcess(SinkJobConfig sinkJobConfig, LineMessage lineMessage) {

    }

    @Override
    public void afterProcess(SinkJobConfig sinkJobConfig, LineMessage lineMessage, Map<String, Object> message) {

    }
}
