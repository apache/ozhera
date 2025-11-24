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
package org.apache.ozhera.log.manager.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.data.push.common.SafeRun;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.mapper.MilogAiConversationMapper;
import org.apache.ozhera.log.manager.model.bo.BotQAParam;
import org.apache.ozhera.log.manager.model.dto.AiAnalysisHistoryDTO;
import org.apache.ozhera.log.manager.model.dto.LogAiAnalysisDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogAiConversationDO;
import org.apache.ozhera.log.manager.model.vo.LogAiAnalysisResponse;
import org.apache.ozhera.log.manager.service.MilogAiAnalysisService;
import org.apache.ozhera.log.manager.service.bot.ContentSimplifyBot;
import org.apache.ozhera.log.manager.service.bot.LogAnalysisBot;
import org.apache.ozhera.log.manager.user.MoneUser;
import run.mone.hive.configs.LLMConfig;
import run.mone.hive.llm.LLM;
import run.mone.hive.llm.LLMProvider;
import run.mone.hive.schema.Message;


import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static run.mone.hive.llm.ClaudeProxy.*;

@Slf4j
@Service
public class MilogAiAnalysisServiceImpl implements MilogAiAnalysisService {

    @Resource
    private LogAnalysisBot analysisBot;

    @Resource
    private ContentSimplifyBot contentSimplifyBot;

    @Resource
    private MilogAiConversationMapper milogAiConversationMapper;

    private static final ConcurrentHashMap<Long, Map<String, List<BotQAParam.QAParam>>> QA_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Long, Object> LOCK = new ConcurrentHashMap<>();

    private static final String MODEL_KEY = "model";
    private static final String ORIGINAL_KEY = "original";

    private static final ConcurrentHashMap<Long, Long> CONVERSATION_TIME = new ConcurrentHashMap<>();
    private static final long CONVERSATION_TIMEOUT = 10 * 60 * 1000;

    private static final Gson gson = new Gson();

    private ScheduledExecutorService scheduledExecutor;

    public void init() {
        LLMConfig config = LLMConfig.builder()
                .llmProvider(LLMProvider.CLAUDE_COMPANY)
                .url(getClaudeUrl())
                .version(getClaudeVersion())
                .maxTokens(getClaudeMaxToekns())
                .build();
        LLM llm = new LLM(config);
        analysisBot.setLlm(llm);
        contentSimplifyBot.setLlm(llm);
        scheduledExecutor = Executors.newScheduledThreadPool(2, ThreadUtil.newNamedThreadFactory("manager-ai-conversation", false));
        scheduledExecutor.scheduleAtFixedRate(() -> SafeRun.run(this::processTask), 0, 2, TimeUnit.MINUTES);
        scheduledExecutor.scheduleAtFixedRate(() -> SafeRun.run(this::checkTokenLength), 0, 1, TimeUnit.MINUTES);
    }


    @Override
    public Result<LogAiAnalysisResponse> tailLogAiAnalysis(LogAiAnalysisDTO tailLogAiAnalysisDTO) {

        if (tailLogAiAnalysisDTO.getStoreId() == null) {
            return Result.failParam("Store id is null");
        }

        if (requestExceedLimit(tailLogAiAnalysisDTO.getLogs())) {
            return Result.failParam("The length of the input information reaches the maximum limit");
        }

        MoneUser user = MoneUserContext.getCurrentUser();
        LogAiAnalysisResponse response = new LogAiAnalysisResponse();
        Long conversationId;
        if (tailLogAiAnalysisDTO.getConversationId() == null) {
            String answer = "";
            try {
                BotQAParam param = new BotQAParam();
                param.setLatestQuestion(gson.toJson(tailLogAiAnalysisDTO.getLogs()));
                String paramJson = gson.toJson(param);
                analysisBot.getRc().news.put(Message.builder().content(paramJson).build());
                Message result = analysisBot.run().join();
                answer = result.getContent();
            } catch (Exception e) {
                log.error("An error occurred in the request for the large model， err: {}", e.getMessage());
                return Result.fail(CommonError.SERVER_ERROR.getCode(), "An error occurred in the request for the large model");
            }

            BotQAParam.QAParam conversation = new BotQAParam.QAParam();
            long timestamp = System.currentTimeMillis();
            String nowTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            conversation.setTime(nowTimeStr);
            conversation.setUser(gson.toJson(tailLogAiAnalysisDTO.getLogs()));
            conversation.setBot(answer);

            List<BotQAParam.QAParam> ModelHistory = new ArrayList<>();
            List<BotQAParam.QAParam> OriginalHistory = new ArrayList<>();
            ModelHistory.add(conversation);
            OriginalHistory.add(conversation);
            //The first request will be created
            MilogAiConversationDO conversationDO = new MilogAiConversationDO();
            conversationDO.setStoreId(tailLogAiAnalysisDTO.getStoreId());
            conversationDO.setCreator(user.getUser());
            conversationDO.setConversationContext(gson.toJson(ModelHistory));
            conversationDO.setOriginalConversation(gson.toJson(OriginalHistory));

            conversationDO.setCreateTime(timestamp);
            conversationDO.setUpdateTime(timestamp);
            conversationDO.setConversationName("新对话 " + nowTimeStr);
            milogAiConversationMapper.insert(conversationDO);
            conversationId = conversationDO.getId();
            Map<String, List<BotQAParam.QAParam>> cache = new HashMap<>();
            cache.put(MODEL_KEY, ModelHistory);
            cache.put(ORIGINAL_KEY, OriginalHistory);
            QA_CACHE.put(conversationId, cache);
            response.setConversationId(conversationId);
            response.setContent(answer);
            CONVERSATION_TIME.put(conversationId, timestamp);
            return Result.success(response);
        } else {
            String answer = "";
            conversationId = tailLogAiAnalysisDTO.getConversationId();
            //This is not first request, need lock
            Object lock = LOCK.computeIfAbsent(conversationId, k -> new Object());
            synchronized (lock) {
                Map<String, List<BotQAParam.QAParam>> cache = QA_CACHE.get(conversationId);
                if (cache == null || cache.isEmpty()) {
                    cache = getHistoryFromDb(conversationId);
                }
                List<BotQAParam.QAParam> modelHistory = cache.get(MODEL_KEY);
                List<BotQAParam.QAParam> originalHistory = cache.get(ORIGINAL_KEY);
                try {
                    BotQAParam param = new BotQAParam();
                    param.setHistoryConversation(modelHistory);
                    param.setLatestQuestion(gson.toJson(tailLogAiAnalysisDTO.getLogs()));
                    String paramJson = gson.toJson(param);
                    analysisBot.getRc().news.put(Message.builder().content(paramJson).build());
                    Message result = analysisBot.run().join();
                    answer = result.getContent();

                } catch (InterruptedException e) {
                    log.error("An error occurred in the request for the large model， err: {}", e.getMessage());
                    return Result.fail(CommonError.SERVER_ERROR.getCode(), "An error occurred in the request for the large model");
                }
                BotQAParam.QAParam conversation = new BotQAParam.QAParam();
                conversation.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                conversation.setUser(gson.toJson(tailLogAiAnalysisDTO.getLogs()));
                conversation.setBot(answer);
                modelHistory.add(conversation);
                originalHistory.add(conversation);
                cache.put(MODEL_KEY, modelHistory);
                cache.put(ORIGINAL_KEY, originalHistory);
                QA_CACHE.put(conversationId, cache);
                CONVERSATION_TIME.put(conversationId, System.currentTimeMillis());
            }
            LOCK.remove(conversationId);
            response.setConversationId(conversationId);
            response.setContent(answer);
            return Result.success(response);
        }

    }


    private Map<String, List<BotQAParam.QAParam>> getHistoryFromDb(Long conversationId) {
        MilogAiConversationDO milogAiConversationDO = milogAiConversationMapper.selectById(conversationId);
        String conversationContext = milogAiConversationDO.getConversationContext();

        if (conversationContext == null || conversationContext.isBlank()) {
            return Collections.emptyMap();
        }

        List<BotQAParam.QAParam> modelConversation = gson.fromJson(conversationContext, new TypeToken<List<BotQAParam.QAParam>>() {
        }.getType());
        List<BotQAParam.QAParam> originalConversation = gson.fromJson(milogAiConversationDO.getOriginalConversation(), new TypeToken<List<BotQAParam.QAParam>>() {
        }.getType());

        Map<String, List<BotQAParam.QAParam>> res = new HashMap<>();
        res.put(MODEL_KEY, modelConversation);
        res.put(ORIGINAL_KEY, originalConversation);
        return res;
    }


    private void processTask() {
        for (Map.Entry<Long, Long> entry : CONVERSATION_TIME.entrySet()) {
            Long conversationId = entry.getKey();
            Object lock = LOCK.computeIfAbsent(conversationId, k -> new Object());
            synchronized (lock) {
                saveHistory(conversationId);
                //It has not been operated for a long time
                if (System.currentTimeMillis() - entry.getValue() > CONVERSATION_TIMEOUT) {
                    CONVERSATION_TIME.remove(entry.getKey());
                    log.info("clean timeout conversation: {}", entry.getKey());
                }
            }
            LOCK.remove(conversationId);
        }
    }

    private void saveHistory(Long conversationId) {
        log.info("开始存入数据库, id : {}", conversationId);
        MilogAiConversationDO milogAiConversationDO = milogAiConversationMapper.selectById(conversationId);
        Map<String, List<BotQAParam.QAParam>> map = QA_CACHE.get(conversationId);
        List<BotQAParam.QAParam> modelHistory = map.get(MODEL_KEY);
        List<BotQAParam.QAParam> originalHistory = map.get(ORIGINAL_KEY);
        milogAiConversationDO.setUpdateTime(System.currentTimeMillis());
        milogAiConversationDO.setConversationContext(gson.toJson(modelHistory));
        milogAiConversationDO.setOriginalConversation(gson.toJson(originalHistory));
        milogAiConversationMapper.updateById(milogAiConversationDO);
    }


    private void checkTokenLength() {
        for (Map.Entry<Long, Map<String, List<BotQAParam.QAParam>>> entry : QA_CACHE.entrySet()) {
            Long conversationId = entry.getKey();
            Object lock = LOCK.computeIfAbsent(conversationId, k -> new Object());
            synchronized (lock) {
                List<BotQAParam.QAParam> originalHistory = entry.getValue().get(ORIGINAL_KEY);
                Integer index = compressIndex(entry.getValue());
                if (index > 0) {
                    List<BotQAParam.QAParam> needCompress = originalHistory.subList(0, index);
                    List<BotQAParam.QAParam> unchangeList = originalHistory.subList(index, originalHistory.size());
                    String res = "";
                    try {
                        contentSimplifyBot.getRc().news.put(Message.builder().content(gson.toJson(needCompress)).build());
                        Message result = contentSimplifyBot.run().join();
                        res = result.getContent();
                    } catch (Exception e) {
                        log.error("An error occurred when requesting the large model to compress data");
                    }
                    if (!res.isBlank()) {
                        List<BotQAParam.QAParam> compressedList = gson.fromJson(res, new TypeToken<List<BotQAParam.QAParam>>() {
                        }.getType());
                        compressedList.addAll(unchangeList);
                        entry.getValue().put(MODEL_KEY, compressedList);
                        QA_CACHE.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            LOCK.remove(conversationId);
        }
    }

    private static String formatString(BotQAParam param) {
        StringBuilder sb = new StringBuilder();
        List<BotQAParam.QAParam> historyConversation = param.getHistoryConversation();
        if (historyConversation != null && !historyConversation.isEmpty()) {
            sb.append("历史对话:\n");
            historyConversation.forEach(h -> {
                sb.append(String.format("[%s] ###用户: %s  ###助手: %s\n", h.getTime(), h.getUser(), h.getBot()));
            });
        }
        sb.append("最新问题: \n ###用户: ").append(param.getLatestQuestion());
        return sb.toString()
                .replaceAll("[\u0000-\u001F]", "")
                .replaceAll("[\\\\\"]", "");

    }


    private static Boolean requestExceedLimit(List<String> logs) {
        String request = gson.toJson(logs);
        if (request.length() >= 20000) {
            return true;
        }
        return false;
    }

    private static Integer compressIndex(Map<String, List<BotQAParam.QAParam>> map) {
        List<BotQAParam.QAParam> paramList = map.get(MODEL_KEY);
        if (gson.toJson(paramList).length() <= 40000) {
            return 0;
        }
       int limit = 20000;
        List<BotQAParam.QAParam> originalList = map.get(ORIGINAL_KEY);
        int sum = 0;
        int index = originalList.size();
        for (int i = originalList.size() - 1; i >= 0; i--) {
            BotQAParam.QAParam param = originalList.get(i);
            String str = gson.toJson(param);
            sum += str.length();
            index = i;
            if (sum >= limit) {
                break;
            }
        }
        int maxCompress = originalList.size() - 20;
        return Math.max(index, maxCompress);
    }


    @Override
    public void shutdown() {
        if (!QA_CACHE.isEmpty()){
            log.info("The project is closed and the cache is flushed to the disk");
            for (Map.Entry<Long, Map<String, List<BotQAParam.QAParam>>> entry : QA_CACHE.entrySet()) {
                MilogAiConversationDO milogAiConversationDO = milogAiConversationMapper.selectById(entry.getKey());
                List<BotQAParam.QAParam> modelHistory = entry.getValue().get(MODEL_KEY);
                List<BotQAParam.QAParam> originalHistory = entry.getValue().get(ORIGINAL_KEY);
                milogAiConversationDO.setUpdateTime(System.currentTimeMillis());
                milogAiConversationDO.setConversationContext(gson.toJson(modelHistory));
                milogAiConversationDO.setOriginalConversation(gson.toJson(originalHistory));
                milogAiConversationMapper.updateById(milogAiConversationDO);
            }
        }
    }

    @Override
    public Result<List<AiAnalysisHistoryDTO>> getAiHistoryList(Long storeId) {
        MoneUser user = MoneUserContext.getCurrentUser();
        List<MilogAiConversationDO> historyList = milogAiConversationMapper.getListByUserAndStore(storeId, user.getUser());
        List<AiAnalysisHistoryDTO> result = new ArrayList<>();
        if(!historyList.isEmpty()){
            result = historyList.stream().map(h -> {
                AiAnalysisHistoryDTO dto = new AiAnalysisHistoryDTO();
                dto.setId(h.getId());
                dto.setName(h.getConversationName());
                dto.setCreateTime(timestampToStr(h.getCreateTime()));
                return dto;
            }).toList();
        }
        return Result.success(result);
    }

    @Override
    public Result<List<BotQAParam.QAParam>> getAiConversation(Long id) {
        Map<String, List<BotQAParam.QAParam>> stringListMap = QA_CACHE.get(id);
        if (stringListMap != null && !stringListMap.isEmpty()) {
            List<BotQAParam.QAParam> paramList = stringListMap.get(ORIGINAL_KEY);
            return Result.success(paramList);
        }
        MilogAiConversationDO conversationDO = milogAiConversationMapper.selectById(id);
        String originalConversationStr = conversationDO.getOriginalConversation();
        List<BotQAParam.QAParam> res =  gson.fromJson(originalConversationStr, new TypeToken<List<BotQAParam.QAParam>>() {}.getType());
        return Result.success(res);
    }

    @Override
    public Result<Boolean> deleteAiConversation(Long id) {
        milogAiConversationMapper.deleteById(id);
        QA_CACHE.remove(id);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> updateAiName(Long id, String name) {
        MilogAiConversationDO conversationDO = milogAiConversationMapper.selectById(id);
        conversationDO.setConversationName(name);
        conversationDO.setUpdateTime(System.currentTimeMillis());
        milogAiConversationMapper.updateById(conversationDO);
        return Result.success(true);
    }

    @Override
    public Result<Boolean> closeAiAnalysis(Long id) {
        Map<String, List<BotQAParam.QAParam>> stringListMap = QA_CACHE.get(id);
        if (stringListMap != null && !stringListMap.isEmpty()) {
            MilogAiConversationDO conversationDO = milogAiConversationMapper.selectById(id);
            conversationDO.setUpdateTime(System.currentTimeMillis());
            conversationDO.setConversationContext(gson.toJson(stringListMap.get(MODEL_KEY)));
            conversationDO.setOriginalConversation(gson.toJson(stringListMap.get(ORIGINAL_KEY)));
            milogAiConversationMapper.updateById(conversationDO);
            QA_CACHE.remove(id);
        }
        return Result.success(true);
    }

    private static String timestampToStr(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return dateTime.format(formatter);
    }
}
