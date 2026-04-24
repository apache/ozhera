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
import org.apache.ozhera.log.manager.model.bo.BotQAParam;
import org.apache.ozhera.log.manager.model.dto.AiAnalysisHistoryDTO;
import org.apache.ozhera.log.manager.model.dto.LogAiAnalysisDTO;
import org.apache.ozhera.log.manager.model.vo.LogAiAnalysisResponse;

import java.util.List;

public interface MilogAiAnalysisService {

    Result<LogAiAnalysisResponse> tailLogAiAnalysis(LogAiAnalysisDTO tailLogAiAnalysisDTO);

    void shutdown();

    Result<List<AiAnalysisHistoryDTO>> getAiHistoryList(Long storeId);

    Result<List<BotQAParam.QAParam>> getAiConversation(Long id);

    Result<Boolean> deleteAiConversation(Long id);

    Result<Boolean> updateAiName(Long id, String name);

    Result<Boolean> closeAiAnalysis(Long id);

    /**
     * Clean up expired AI conversation records
     * Deletes conversations that have not been updated for more than 7 days
     */
    void cleanExpiredConversations();

}
