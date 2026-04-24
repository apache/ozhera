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

package org.apache.ozhera.log.manager.service.extension.space;

import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.common.Config;

import static org.apache.ozhera.log.manager.service.extension.space.SpaceExtensionService.DEFAULT_SPACE_EXTENSION_SERVICE_KEY;

@Slf4j
public class SpaceExtensionServiceFactory {
    private static String factualServiceName;

    public static SpaceExtensionService getSpaceExtensionService() {
        factualServiceName = Config.ins().get("space.extension.service", DEFAULT_SPACE_EXTENSION_SERVICE_KEY);
        log.debug("SpaceExtensionServiceFactory factualServiceName:{}", factualServiceName);
        return Ioc.ins().getBean(factualServiceName);
    }
}