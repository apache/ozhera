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
package com.xiaomi.mone.log.agent.service;

import com.xiaomi.mone.log.agent.export.MsgExporter;
import com.xiaomi.mone.log.agent.output.Output;
import com.xiaomi.mone.log.api.model.meta.LogPattern;

public interface OutPutService {
    /**
     * comparison of old and new configurations to determine the life of production
     *
     * @param oldOutput
     * @param newOutput
     * @return
     */
    boolean compare(Output oldOutput, Output newOutput);

    /**
     * configuration check
     *
     * @param output
     */
    void preCheckOutput(Output output);

    /**
     * initialize message exposer
     *
     * @param output
     * @return
     * @throws Exception
     */
    MsgExporter exporterTrans(Output output) throws Exception;

    /**
     * remove message producer based on configuration
     *
     * @param output
     */
    void removeMQ(Output output);

    /**
     * get Output according to Log Pattern
     *
     * @param logPattern
     * @return
     */
    Output configOutPut(LogPattern logPattern);
}
