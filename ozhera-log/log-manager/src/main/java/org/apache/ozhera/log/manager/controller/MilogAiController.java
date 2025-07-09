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
package org.apache.ozhera.log.manager.controller;

import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import com.xiaomi.youpin.docean.anno.RequestParam;
import org.apache.ozhera.log.manager.model.bo.BotQAParam;
import org.apache.ozhera.log.manager.model.dto.AiAnalysisHistoryDTO;
import org.apache.ozhera.log.manager.model.dto.LogAiAnalysisDTO;
import org.apache.ozhera.log.manager.model.vo.LogAiAnalysisResponse;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.service.MilogAiAnalysisService;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class MilogAiController {

    @Resource
    private MilogAiAnalysisService milogAiAnalysisService;

    @RequestMapping(path = "/milog/tail/aiAnalysis", method = "post")
    public Result<LogAiAnalysisResponse> aiAnalysis(LogAiAnalysisDTO tailLogAiAnalysisDTO) {
        return milogAiAnalysisService.tailLogAiAnalysis(tailLogAiAnalysisDTO);
    }

    @RequestMapping(path = "/milog/tail/aiHistoryList", method = "post")
    public Result<List<AiAnalysisHistoryDTO>> getAiHistoryList(@RequestParam(value = "storeId") Long storeId){
        return milogAiAnalysisService.getAiHistoryList(storeId);
    }

    @RequestMapping(path = "/milog/tail/aiConversation", method = "post")
    public Result<List<BotQAParam.QAParam>> getAiConversation(@RequestParam(value = "id") Long id){
        return milogAiAnalysisService.getAiConversation(id);
    }

    @RequestMapping(path = "/milog/tail/deleteAiConversation", method = "post")
    public Result<Boolean> deleteAiConversation(@RequestParam(value = "id") Long id){
        return milogAiAnalysisService.deleteAiConversation(id);
    }

    @RequestMapping(path = "/milog/tail/updateAiName", method = "post")
    public Result<Boolean> updateAiName(@RequestParam(value = "id") Long id, @RequestParam(value = "name") String name){
        return milogAiAnalysisService.updateAiName(id, name);
    }

    @RequestMapping(path = "/milog/tail/closeAiAnalysis", method = "post")
    public Result<Boolean> closeAiAnalysis(@RequestParam(value = "id") Long id){
        return milogAiAnalysisService.closeAiAnalysis(id);
    }

}
