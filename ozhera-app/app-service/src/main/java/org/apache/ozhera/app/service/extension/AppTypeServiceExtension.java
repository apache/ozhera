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
package org.apache.ozhera.app.service.extension;

public interface AppTypeServiceExtension {
    /**
     * 日志类型转化为平台类型
     *
     * @param type
     * @return
     */
    Integer getAppTypeLog(Integer type);

    Integer getAppPlatForm(Integer type);

    /**
     * 得到日志类型
     *
     * @param type
     * @return
     */
    Integer getAppTypePlatformType(Integer type);


    String getPlatformName(Integer platformType);

    String getAppTypeName(Integer appType);
}
