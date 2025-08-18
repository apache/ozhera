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
package org.apache.ozhera.log.agent.service.impl;

import org.apache.ozhera.log.agent.factory.OutPutServiceFactory;
import org.apache.ozhera.log.agent.output.Output;
import org.apache.ozhera.log.agent.service.ChannelDefineLocatorExtension;
import org.apache.ozhera.log.api.enums.MQSourceEnum;
import org.apache.ozhera.log.api.model.meta.LogPattern;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Date 2023/4/27 12:20 PM
 */
@Service(name = "OuterChannelDefineLocatorExtensionImpl")
@Slf4j
public class OuterChannelDefineLocatorExtensionImpl implements ChannelDefineLocatorExtension {
    @Override
    public Output getOutPutByMQConfigType(LogPattern logPattern) {
        if (null != logPattern.getMQConfig()) {
            String typeName = logPattern.getMQConfig().getType();
            MQSourceEnum middlewareEnum = MQSourceEnum.queryByName(typeName);
            if (null != middlewareEnum) {
                return OutPutServiceFactory.getOutPutService(middlewareEnum.getServiceName()).configOutPut(logPattern);
            }
        }
        return null;
    }
}
