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
package org.apache.ozhera.log.manager.service.path;

import org.apache.ozhera.log.manager.model.vo.LogAgentListBo;
import com.xiaomi.youpin.docean.anno.Service;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description The mone log path is based on docker, and the path has not changed
 * @date 2022/11/15 18:51
 */
@Service
public class MoneLogPathMapping implements LogPathMapping {
    @Override
    public String getLogPath(String originLogPath, List<LogAgentListBo> logAgentListBos) {
        return originLogPath;
    }
}
