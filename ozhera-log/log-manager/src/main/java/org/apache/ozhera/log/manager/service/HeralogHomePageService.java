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
package org.apache.ozhera.log.manager.service;

import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.model.dto.MilogSpaceTreeDTO;
import org.apache.ozhera.log.manager.model.dto.UnAccessAppDTO;
import org.apache.ozhera.log.manager.model.dto.ValueDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;

import java.util.List;
import java.util.Map;


public interface HeralogHomePageService {
    
    /**
     * Get all apps info
     *
     * @return
     */
    Result<Map<String, Object>> milogAccess();
    
    /**
     * Package and return all application details.
     *
     * @return
     */
    Result<List<UnAccessAppDTO>> unAccessAppList();
    
    /**
     * Get all logStoreInfo by spaceId and return in a tree structure.
     *
     * @param spaceId
     * @return
     */
    Result<List<MilogSpaceTreeDTO>> getMilogSpaceTree(Long spaceId);
    
    /**
     * Query the store that originally belonged to the space, and query the authorized store
     *
     * @param spaceId
     * @return
     */
    List<MilogLogStoreDO> getMilogLogStoreDOS(Long spaceId);
    
    /**
     * Get log pattern from properties
     *
     * @return
     */
    Result<List<ValueDTO<String>>> getMiloglogAccessPattern();
}
