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
package com.xiaomi.youpin.prometheus.agent.Impl;

import com.xiaomi.youpin.prometheus.agent.entity.RuleSilenceEntity;
import com.xiaomi.youpin.prometheus.agent.entity.ScrapeConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author zhangxiaowei6
 * @Date 2023/10/17 14:28
 */
@Slf4j
@Repository
public class SilenceDao  extends BaseDao{

    public Long CreateSilence(RuleSilenceEntity entity) {
        return dao.insert(entity).getId();
    }
}
