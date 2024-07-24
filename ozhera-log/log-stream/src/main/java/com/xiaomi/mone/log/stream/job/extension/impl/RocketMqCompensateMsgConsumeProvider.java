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

import com.xiaomi.mone.log.stream.common.LogStreamConstants;
import com.xiaomi.mone.log.stream.job.extension.CompensateMsgConsume;
import com.xiaomi.mone.log.stream.job.extension.CompensateMsgConsumeProvider;
import com.xiaomi.youpin.docean.anno.Service;

/**
 * @author shanwb
 * @date 2023-04-10
 */
@Service(name = "rocketmq" + LogStreamConstants.compensateMsgConsumeProviderBeanSuffix)
public class RocketMqCompensateMsgConsumeProvider implements CompensateMsgConsumeProvider {

    @Override
    public CompensateMsgConsume getCompensateMsgConsume() {
        CompensateMsgConsume mqMessageConsume = new RocketCompensateMsgConsume();
        return  mqMessageConsume;
    }

}
