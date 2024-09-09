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
package com.xiaomi.mone.log.agent.export;

import com.google.gson.Gson;
import com.xiaomi.mone.log.agent.channel.Closeable;
import com.xiaomi.mone.log.api.enums.LogTypeEnum;
import com.xiaomi.mone.log.api.model.msg.LineMessage;

import java.util.List;

/**
 * @author shanwb
 * @date 2021-07-19
 * @describe Exposed encapsulation of log messages
 */
public interface MsgExporter extends Closeable {

    String OPENTELEMETRY_TYPE = String.valueOf(LogTypeEnum.OPENTELEMETRY.getType());

    Gson gson = new Gson();

    int BATCH_EXPORT_SIZE = 200;

    /**
     * Single export
     *
     * @param message
     */
    void export(LineMessage message);

    /**
     * Multiple exports
     *
     * @param messageList
     */
    void export(List<LineMessage> messageList);

    /**
     * Batch export quantity
     *
     * @return
     */
    default int batchExportSize() {
        return BATCH_EXPORT_SIZE;
    }
}
