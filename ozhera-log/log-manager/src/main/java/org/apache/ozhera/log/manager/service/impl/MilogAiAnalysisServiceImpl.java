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


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingType;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.common.Config;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.config.redis.RedisClientFactory;
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
import redis.clients.jedis.*;
import redis.clients.jedis.params.SetParams;
import run.mone.hive.configs.LLMConfig;
import run.mone.hive.llm.LLM;
import run.mone.hive.llm.LLMProvider;
import run.mone.hive.schema.Message;
import run.mone.hive.schema.MetaKey;
import run.mone.hive.schema.MetaValue;


import javax.annotation.Resource;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MINUTES;


@Slf4j
@Service
public class MilogAiAnalysisServiceImpl implements MilogAiAnalysisService {

    @Resource
    private LogAnalysisBot analysisBot;

    @Resource
    private ContentSimplifyBot contentSimplifyBot;

    @Resource
    private MilogAiConversationMapper milogAiConversationMapper;

    private static final String MODEL_KEY = "model";
    private static final String ORIGINAL_KEY = "original";

    private static final String MILOG_AI_KEY_PREFIX = "milog.ai.conversation:";

    private static final String LOCK_PREFIX = "milog.ai.lock:";

    private static final String GLOBAL_CHECK_LOCK_KEY = "milog.ai.checkTokenLength:global";

    private static final String GLOBAL_SHUTDOWN_LOCK_KEY = "milog.ai.shutdown:global";

    private static final String GLOBAL_CLEAN_EXPIRED_LOCK_KEY = "milog.ai.cleanExpired:global";

    private static final int CONVERSATION_EXPIRE_DAYS = 7;

    private static final Gson gson = new Gson();

    private static final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private static final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    private static final JedisCluster jedisCluster = RedisClientFactory.getJedisCluster();

    private static final Encoding TOKENIZER = Encodings.newDefaultEncodingRegistry().getEncoding(EncodingType.CL100K_BASE);

    public void init() {
        String llmUrl = Config.ins().get("llm.url", "");
        String llmToken = Config.ins().get("llm.token", "");
        LLMConfig config = LLMConfig.builder()
                .url(llmUrl)
                .token(llmToken)
                .llmProvider(LLMProvider.MIFY_GATEWAY)
                .build();
        LLM llm = new LLM(config);
        llm.setConfigFunction(llmProvider -> Optional.of(config));
        analysisBot.setLlm(llm);
        contentSimplifyBot.setLlm(llm);

        // Schedule cleanup task to run at 3:00 AM every day
        long initialDelay = calculateDelayToTargetHour(3);
        cleanupScheduler.scheduleAtFixedRate(() -> {
            try {
                cleanExpiredConversations();
            } catch (Exception e) {
                log.error("Scheduled cleanup task failed", e);
            }
        }, initialDelay, 24 * 60, MINUTES);
        log.info("Scheduled AI conversation cleanup task initialized, will run at 3:00 AM every day, initial delay: {} minutes", initialDelay);
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
                param.setLatestQuestion(formatLogs(tailLogAiAnalysisDTO.getLogs()));
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
            conversation.setUser(formatLogs(tailLogAiAnalysisDTO.getLogs()));
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
            putCache(conversationId, cache);
            response.setConversationId(conversationId);
            response.setContent(answer);
            return Result.success(response);
        } else {
            conversationId = tailLogAiAnalysisDTO.getConversationId();
            //This is not first request, need lock

            Map<String, List<BotQAParam.QAParam>> cache = getConversation(conversationId);
            List<BotQAParam.QAParam> modelHistory = cache.get(MODEL_KEY);
            List<BotQAParam.QAParam> originalHistory = cache.get(ORIGINAL_KEY);
            AnalysisResult analysisResult = processHistoryConversation(conversationId, cache, tailLogAiAnalysisDTO);
            String answer = analysisResult.getAnswer();
            BotQAParam.QAParam conversation = new BotQAParam.QAParam();
            conversation.setTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            conversation.setUser(formatLogs(tailLogAiAnalysisDTO.getLogs()));
            conversation.setBot(answer);
            if (analysisResult.getCompressedModelHistory() != null) {
                List<BotQAParam.QAParam> compressedModelHistory = analysisResult.getCompressedModelHistory();
                compressedModelHistory.add(conversation);
                cache.put(MODEL_KEY, compressedModelHistory);
            } else {
                modelHistory.add(conversation);
                cache.put(MODEL_KEY, modelHistory);
            }
            originalHistory.add(conversation);
            cache.put(ORIGINAL_KEY, originalHistory);
            putCache(conversationId, cache);
            response.setConversationId(conversationId);
            response.setContent(answer);
            return Result.success(response);
        }

    }

    private AnalysisResult processHistoryConversation(Long conversationId, Map<String, List<BotQAParam.QAParam>> cache, LogAiAnalysisDTO tailLogAiAnalysisDTO) {
        List<BotQAParam.QAParam> modelHistory = cache.get(MODEL_KEY);
        List<BotQAParam.QAParam> originalHistory = cache.get(ORIGINAL_KEY);
        AnalysisResult res = new AnalysisResult();
        try {
            BotQAParam param = new BotQAParam();
            param.setHistoryConversation(modelHistory);
            param.setLatestQuestion(formatLogs(tailLogAiAnalysisDTO.getLogs()));
            String paramJson = gson.toJson(param);
            if (TOKENIZER.countTokens(paramJson) < 70000) {
                analysisBot.getRc().news.put(Message.builder().content(paramJson).build());
                Message result = analysisBot.run().join();
                String answer = result.getContent();
                res.setAnswer(answer);
                return res;
            } else {
                return analysisAndCompression(modelHistory, originalHistory, tailLogAiAnalysisDTO.getLogs(), conversationId);
            }
        } catch (InterruptedException e) {
            log.error("An error occurred in the request for the large model， err: {}", e.getMessage());
        }
        return res;
    }

    private AnalysisResult analysisAndCompression(List<BotQAParam.QAParam> modelHistory, List<BotQAParam.QAParam> originalHistory, List<String> latestConversation, Long conversationId) {
        AnalysisResult analysisResult = new AnalysisResult();

        AtomicReference<String> answer = new AtomicReference<>("");
        Future<?> analysisFuture = executor.submit(() -> {
            try {
                BotQAParam param = new BotQAParam();
                param.setHistoryConversation(modelHistory);
                param.setLatestQuestion(gson.toJson(latestConversation));
                String paramJson = gson.toJson(param);
                analysisBot.getRc().news.put(Message.builder().content(paramJson).build());
                Message result = analysisBot.run().join();
                answer.set(result.getContent());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        AtomicReference<List<BotQAParam.QAParam>> newModelHistory = new AtomicReference<>(modelHistory);
        Future<?> compressionFuture = executor.submit(() -> {
            int index = compressIndex(modelHistory, originalHistory);
            if (index <= 0) {
                newModelHistory.set(modelHistory);
                return;
            }
            List<BotQAParam.QAParam> needCompress = new ArrayList<>(originalHistory.subList(0, index));
            List<BotQAParam.QAParam> unchangeList = new ArrayList<>(originalHistory.subList(index, originalHistory.size()));

            String needCompressJson = gson.toJson(needCompress);
            //Compress the content that needs to be compressed to have the same number of tokens as the content that does not need to be compressed, as much as possible.

            int currentTokenCount = TOKENIZER.countTokens(needCompressJson);
            int targetTokenCount = TOKENIZER.countTokens(gson.toJson(unchangeList));

            Map<MetaKey, MetaValue> meta = new HashMap<>();
            MetaKey currentKey = MetaKey.builder().key("currentCount").desc("currentCount").build();
            MetaValue currentValue = MetaValue.builder().value(currentTokenCount).desc("currentCount").build();
            meta.put(currentKey, currentValue);

            MetaKey targetKey = MetaKey.builder().key("targetCount").desc("targetCount").build();
            MetaValue targetValue = MetaValue.builder().value(targetTokenCount).desc("targetCount").build();
            meta.put(targetKey, targetValue);
            String res;
            try {
                contentSimplifyBot.getRc().news.put(
                        Message.builder().content(needCompressJson).meta(meta).build());
                Message result = contentSimplifyBot.run().join();
                res = result.getContent();
            } catch (Exception e) {
                log.error("An error occurred when requesting the large model to compress data, error: {}", e.getMessage());
                return;
            }
            if (res == null || res.isBlank()) {
                return;
            }

            List<BotQAParam.QAParam> compressedList = gson.fromJson(
                    res,
                    new TypeToken<List<BotQAParam.QAParam>>() {
                    }.getType()
            );
            if (compressedList == null || compressedList.isEmpty()) {
                return;
            }
            compressedList.addAll(unchangeList);
            newModelHistory.set(compressedList);
        });
        try {
            analysisFuture.get();
            compressionFuture.get();
            String s = answer.get();
            List<BotQAParam.QAParam> paramList = newModelHistory.get();
            analysisResult.setAnswer(s);
            analysisResult.setCompressedModelHistory(paramList);
        } catch (Exception e) {
            log.error("analysis and compression of task execution error: {}", e.getMessage());
        }
        return analysisResult;
    }

    private void checkTokenLength() {
        if (!trySimpleLock(GLOBAL_CHECK_LOCK_KEY, 50L)) {
            return;
        }

        Set<String> allCacheKey = getAllCacheKey();
        if (allCacheKey.isEmpty()) {
            return;
        }
        for (String key : allCacheKey) {
            executor.submit(() -> {
                String[] split = key.split(":");
                String uuid = UUID.randomUUID().toString();
                if (split.length == 0) {
                    return;
                }
                Long conversationId;
                try {
                    conversationId = Long.valueOf(split[split.length - 1]);
                } catch (NumberFormatException e) {
                    log.warn("invalid conversation key: {}", key);
                    return;
                }
                if (!tryLock(conversationId, uuid, 300L)) {
                    return;
                }

                try {
                    String value = jedisCluster.get(key);
                    if (value == null || value.isEmpty()) {
                        return;
                    }
                    Map<String, List<BotQAParam.QAParam>> map = gson.fromJson(value, new TypeToken<Map<String, List<BotQAParam.QAParam>>>() {
                    }.getType());
                    if (map == null || map.isEmpty()) {
                        return;
                    }
                    int index = compressIndex(map);
                    if (index <= 0) {
                        return;
                    }
                    List<BotQAParam.QAParam> originalHistory = map.get(ORIGINAL_KEY);
                    if (originalHistory == null || originalHistory.size() <= index) {
                        return;
                    }
                    List<BotQAParam.QAParam> needCompress = new ArrayList<>(originalHistory.subList(0, index));
                    List<BotQAParam.QAParam> unchangeList = new ArrayList<>(originalHistory.subList(index, originalHistory.size()));

                    String res;
                    try {
                        contentSimplifyBot.getRc().news.put(
                                Message.builder().content(gson.toJson(needCompress)).build());
                        Message result = contentSimplifyBot.run().join();
                        res = result.getContent();
                    } catch (Exception e) {
                        log.error("An error occurred when requesting the large model to compress data, key: {}, error: {}", key, e.getMessage());
                        return;
                    }
                    if (res == null || res.isBlank()) {
                        return;
                    }

                    List<BotQAParam.QAParam> compressedList = gson.fromJson(
                            res,
                            new TypeToken<List<BotQAParam.QAParam>>() {
                            }.getType()
                    );
                    if (compressedList == null || compressedList.isEmpty()) {
                        return;
                    }
                    compressedList.addAll(unchangeList);
                    map.put(MODEL_KEY, compressedList);
                    jedisCluster.setex(key, 60 * 60, gson.toJson(map));
                } catch (Exception e) {
                    log.error("checkTokenLength error for key: {}, error: {}", key, e.getMessage());
                } finally {
                    unLock(conversationId, uuid);
                }

            });
        }

    }

    private static Boolean requestExceedLimit(List<String> logs) {
        String formatLog = formatLogs(logs);
        int count = TOKENIZER.countTokens(formatLog);
        return count > 20000;
    }

    private static Integer compressIndex(Map<String, List<BotQAParam.QAParam>> map) {
        List<BotQAParam.QAParam> paramList = map.get(MODEL_KEY);
        String modelJson = gson.toJson(paramList);
        int count = TOKENIZER.countTokens(modelJson);
        if (count <= 70000) {
            return 0;
        }
        int limit = 20000;
        List<BotQAParam.QAParam> originalList = map.get(ORIGINAL_KEY);
        int sum = 0;
        int index = originalList.size();
        for (int i = originalList.size() - 1; i >= 0; i--) {
            BotQAParam.QAParam param = originalList.get(i);
            String str = gson.toJson(param);
            sum += TOKENIZER.countTokens(str);
            ;
            index = i;
            if (sum >= limit) {
                break;
            }
        }
        int maxCompress = originalList.size() - 20;
        return Math.max(index, maxCompress);
    }

    private static Integer compressIndex(List<BotQAParam.QAParam> paramList, List<BotQAParam.QAParam> originalList) {
        String modelJson = gson.toJson(paramList);
        int count = TOKENIZER.countTokens(modelJson);
        if (count <= 50000) {
            return 0;
        }
        int limit = 20000;
        int sum = 0;
        int index = originalList.size();
        for (int i = originalList.size() - 1; i >= 0; i--) {
            BotQAParam.QAParam param = originalList.get(i);
            String str = gson.toJson(param);
            sum += TOKENIZER.countTokens(str);
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
        if (!trySimpleLock(GLOBAL_SHUTDOWN_LOCK_KEY, 120L)) {
            return;
        }
        Set<String> allCacheKey = getAllCacheKey();
        if (!allCacheKey.isEmpty()) {
            List<Future<?>> futures = new ArrayList<>();
            for (String key : allCacheKey) {
                Future<?> future = executor.submit(() -> {
                    String[] split = key.split(":");
                    Long conversationId = Long.valueOf(split[split.length - 1]);
                    String value = jedisCluster.get(key);
                    Map<String, List<BotQAParam.QAParam>> map = gson.fromJson(value, new TypeToken<Map<String, List<BotQAParam.QAParam>>>() {
                    }.getType());
                    List<BotQAParam.QAParam> modelHistory = map.get(MODEL_KEY);
                    List<BotQAParam.QAParam> originalHistory = map.get(ORIGINAL_KEY);
                    MilogAiConversationDO conversationDO = milogAiConversationMapper.selectById(conversationId);
                    if (conversationDO != null) {
                        conversationDO.setOriginalConversation(gson.toJson(originalHistory));
                        conversationDO.setConversationContext(gson.toJson(modelHistory));
                        milogAiConversationMapper.updateById(conversationDO);
                    }
                });
                futures.add(future);
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    log.error("A future task execute failed: {}", e.getMessage());
                }
            }
        }
    }

    @Override
    public Result<List<AiAnalysisHistoryDTO>> getAiHistoryList(Long storeId) {
        MoneUser user = MoneUserContext.getCurrentUser();
        List<MilogAiConversationDO> historyList = milogAiConversationMapper.getListByUserAndStore(storeId, user.getUser());
        List<AiAnalysisHistoryDTO> result = new ArrayList<>();
        if (!historyList.isEmpty()) {
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
        Map<String, List<BotQAParam.QAParam>> stringListMap = getCache(id);
        if (stringListMap != null && !stringListMap.isEmpty()) {
            List<BotQAParam.QAParam> paramList = stringListMap.get(ORIGINAL_KEY);
            return Result.success(paramList);
        }
        MilogAiConversationDO conversationDO = milogAiConversationMapper.selectById(id);
        String originalConversationStr = conversationDO.getOriginalConversation();
        List<BotQAParam.QAParam> res = gson.fromJson(originalConversationStr, new TypeToken<List<BotQAParam.QAParam>>() {
        }.getType());
        return Result.success(res);
    }

    @Override
    public Result<Boolean> deleteAiConversation(Long id) {
        milogAiConversationMapper.deleteById(id);
        removeCache(id);
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
        Map<String, List<BotQAParam.QAParam>> stringListMap = getCache(id);
        if (stringListMap != null && !stringListMap.isEmpty()) {
            MilogAiConversationDO conversationDO = milogAiConversationMapper.selectById(id);
            conversationDO.setUpdateTime(System.currentTimeMillis());
            conversationDO.setConversationContext(gson.toJson(stringListMap.get(MODEL_KEY)));
            conversationDO.setOriginalConversation(gson.toJson(stringListMap.get(ORIGINAL_KEY)));
            milogAiConversationMapper.updateById(conversationDO);
            removeCache(id);
        }
        return Result.success(true);
    }

    @Override
    public void cleanExpiredConversations() {
        // Use distributed lock to ensure only one instance executes the cleanup
        if (!trySimpleLock(GLOBAL_CLEAN_EXPIRED_LOCK_KEY, 300L)) {
            log.info("Another instance is already running cleanExpiredConversations, skipping...");
            return;
        }

        try {
            // Calculate the expiration timestamp (7 days ago)
            long expireTime = Instant.now().minus(CONVERSATION_EXPIRE_DAYS, ChronoUnit.DAYS).toEpochMilli();

            // Delete expired conversations from database
            int deletedCount = milogAiConversationMapper.deleteByUpdateTimeBefore(expireTime);

            log.info("Cleaned up {} expired AI conversation records (update_time before {})",
                    deletedCount, timestampToStr(expireTime));
        } catch (Exception e) {
            log.error("Failed to clean expired AI conversations", e);
        }
    }

    private static String timestampToStr(long timestamp) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return dateTime.format(formatter);
    }

    /**
     * Calculate the delay in minutes from now to the target hour (e.g., 3:00 AM)
     *
     * @param targetHour the target hour (0-23)
     * @return delay in minutes
     */
    private static long calculateDelayToTargetHour(int targetHour) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetTime = now.withHour(targetHour).withMinute(0).withSecond(0).withNano(0);

        // If the target time has already passed today, schedule for tomorrow
        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusDays(1);
        }

        return ChronoUnit.MINUTES.between(now, targetTime);
    }

    private Map<String, List<BotQAParam.QAParam>> getConversation(Long conversationId) {
        String redisKey = MILOG_AI_KEY_PREFIX + conversationId;
        String value = jedisCluster.get(redisKey);
        if (value != null && !value.isEmpty()) {
            Map<String, List<BotQAParam.QAParam>> map = gson.fromJson(value, new TypeToken<Map<String, List<BotQAParam.QAParam>>>() {
            }.getType());
            return map;
        }

        MilogAiConversationDO conversationDO = milogAiConversationMapper.selectById(conversationId);
        if (conversationDO != null) {
            Map<String, List<BotQAParam.QAParam>> conversationMap = new HashMap<>();
            String conversationContext = conversationDO.getConversationContext();
            List<BotQAParam.QAParam> modelConversation = gson.fromJson(conversationContext, new TypeToken<List<BotQAParam.QAParam>>() {
            }.getType());
            String originalConversationStr = conversationDO.getOriginalConversation();
            List<BotQAParam.QAParam> originalConversation = gson.fromJson(originalConversationStr, new TypeToken<List<BotQAParam.QAParam>>() {
            }.getType());

            conversationMap.put(MODEL_KEY, modelConversation);
            conversationMap.put(ORIGINAL_KEY, originalConversation);

            putCache(conversationId, conversationMap);
            return conversationMap;
        }

        return new HashMap<>();
    }

    private static boolean putCache(Long conversationId, Map<String, List<BotQAParam.QAParam>> map) {
        if (map == null || map.isEmpty()) {
            return false;
        }
        String redisKey = MILOG_AI_KEY_PREFIX + conversationId;
        String res = jedisCluster.setex(redisKey, 60 * 60, gson.toJson(map));
        return true;
    }

    private Map<String, List<BotQAParam.QAParam>> getCache(Long conversationId) {
        String redisKey = MILOG_AI_KEY_PREFIX + conversationId;
        String value = jedisCluster.get(redisKey);
        if (value != null && !value.isEmpty()) {
            Map<String, List<BotQAParam.QAParam>> map = gson.fromJson(value, new TypeToken<Map<String, List<BotQAParam.QAParam>>>() {
            }.getType());
            return map;
        }
        return new HashMap<>();
    }


    private static void removeCache(Long conversationId) {
        String redisKey = MILOG_AI_KEY_PREFIX + conversationId;
        jedisCluster.del(redisKey);
    }

    private static String formatLogs(List<String> logs) {
        return String.join("\n", logs);
    }

    private Set<String> getAllCacheKey() {
        Set<String> keys = new HashSet<>();
        Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
        for (Map.Entry<String, JedisPool> entry : clusterNodes.entrySet()) {
            try (Jedis jedis = entry.getValue().getResource()) {
                String cursor = "0";
                do {
                    ScanResult<String> scanResult = jedis.scan(cursor, new ScanParams().match(MILOG_AI_KEY_PREFIX + "*").count(1000));
                    keys.addAll(scanResult.getResult());
                    cursor = scanResult.getCursor();
                } while (!cursor.equals("0"));
            } catch (Exception e) {
                log.error("Failed to retrieve all conversation keys from Redis!");
            }
        }
        return keys;
    }

    private boolean tryLock(Long conversationId, String value, Long expireSeconds) {
        String lockKey = LOCK_PREFIX + conversationId;
        SetParams params = new SetParams();
        params.nx();
        params.px(expireSeconds * 1000);
        String res = jedisCluster.set(lockKey, value, params);
        return "OK".equals(res);
    }

    private boolean trySimpleLock(String key, Long expireSeconds) {
        SetParams params = new SetParams();
        params.nx();
        params.px(expireSeconds * 1000);
        String res = jedisCluster.set(key, "1", params);
        return "OK".equals(res);
    }

    private static final String UNLOCK_LUA =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "   return redis.call('del', KEYS[1]) " +
                    "else " +
                    "   return 0 " +
                    "end";

    private void unLock(Long conversationId, String value) {
        String lockKey = LOCK_PREFIX + conversationId;
        try {
            jedisCluster.eval(UNLOCK_LUA, Collections.singletonList(lockKey), Collections.singletonList(value));

        } catch (Exception e) {
            log.error("failed to unlock key: {}, error:{}", lockKey, e.getMessage());
        }
    }

    @Data
    static class AnalysisResult {
        private String answer;
        private List<BotQAParam.QAParam> compressedModelHistory;
    }

    @Data
    static class CompressionIndex {
        private Integer index;
        private Integer currentTokenCount;
        private Integer targetTokenCount;
    }

}
