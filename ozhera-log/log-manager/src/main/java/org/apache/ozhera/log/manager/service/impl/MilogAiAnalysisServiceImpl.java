package org.apache.ozhera.log.manager.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.data.push.common.SafeRun;
import com.xiaomi.youpin.infra.rpc.errors.GeneralCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.common.ErrorCode;
import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.model.bo.BotQAParam;
import org.apache.ozhera.log.manager.model.dto.LogAiAnalysisDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogAiConversationDO;
import org.apache.ozhera.log.manager.model.vo.LogAiAnalysisResponse;
import org.apache.ozhera.log.manager.service.MilogAiAnalysisService;
import org.apache.ozhera.log.manager.service.bot.LogAnalysisBot;
import org.apache.ozhera.log.manager.user.MoneUser;
import org.nutz.dao.impl.NutDao;
import org.springframework.stereotype.Service;
import run.mone.hive.configs.LLMConfig;
import run.mone.hive.llm.LLM;
import run.mone.hive.llm.LLMProvider;
import run.mone.hive.schema.Message;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static run.mone.hive.llm.ClaudeProxy.*;

@Service
@Slf4j
public class MilogAiAnalysisServiceImpl implements MilogAiAnalysisService {

    @Resource
    private NutDao dao;

    @Resource
    private LogAnalysisBot analysisBot;

    private static final ConcurrentHashMap<Long, List<BotQAParam.QAParam>> QA_CACHE = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, List<BotQAParam.QAParam>> REFRESH_DESK = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Long, Long> CONVERSATION_TIME = new ConcurrentHashMap<>();
    private static final long CONVERSATION_TIMEOUT = 20 * 60 * 1000;

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
    }


    @Override
    public Result<LogAiAnalysisResponse> tailLogAiAnalysis(LogAiAnalysisDTO tailLogAiAnalysisDTO) {

        if (tailLogAiAnalysisDTO.getStoreId() == null) {
            return Result.failParam("storeId is null");
        }

        MoneUser user = MoneUserContext.getCurrentUser();
        LogAiAnalysisResponse response = new LogAiAnalysisResponse();
        Long conversationId;
        if (tailLogAiAnalysisDTO.getConversationId() == null) {
            String answer = "";
            try {
                BotQAParam param = new BotQAParam();
                param.setLatestQuestion(gson.toJson(tailLogAiAnalysisDTO.getLogs()));
                analysisBot.getRc().news.put(Message.builder().content(gson.toJson(param)).build());
                Message result = analysisBot.run().join();
                answer = result.getContent();
            } catch (Exception e) {
                log.error("An error occurred in the request for the large model， err: {}", e.getMessage());
                return Result.fail(CommonError.SERVER_ERROR.getCode(), "An error occurred in the request for the large model");
            }

            BotQAParam.QAParam conversation = new BotQAParam.QAParam();
            conversation.setUser(gson.toJson(tailLogAiAnalysisDTO.getLogs()));
            conversation.setBot(answer);

            List<BotQAParam.QAParam> history = new ArrayList<>();
            history.add(conversation);

            MilogAiConversationDO conversationDO = new MilogAiConversationDO();
            conversationDO.setStoreId(tailLogAiAnalysisDTO.getStoreId());
            conversationDO.setCreator(user.getUser());
            conversationDO.setConversationContext(gson.toJson(history));
            conversationDO.setOriginalConversation(gson.toJson(history));
            long timestamp = System.currentTimeMillis();
            conversationDO.setCreateTime(timestamp);
            conversationDO.setUpdateTime(timestamp);
            MilogAiConversationDO insertDo = dao.insert(conversationDO);
            conversationId = insertDo.getId();

            QA_CACHE.put(conversationId, history);
            response.setConversationId(conversationId);
            response.setContent(answer);
            CONVERSATION_TIME.put(conversationId, timestamp);
            return Result.success(response);
        } else {
            String answer = "";
            conversationId = tailLogAiAnalysisDTO.getConversationId();
            List<BotQAParam.QAParam> historyCache = QA_CACHE.get(conversationId);
            if (historyCache == null || historyCache.isEmpty()) {
                historyCache = getHistoryFromDb(conversationId);
            }
            List<BotQAParam.QAParam> history = new ArrayList<>(historyCache);
            try {
                BotQAParam param = new BotQAParam();
                param.setHistoryConversation(history);
                param.setLatestQuestion(gson.toJson(tailLogAiAnalysisDTO.getLogs()));
                analysisBot.getRc().news.put(Message.builder().content(gson.toJson(param)).build());
                Message result = analysisBot.run().join();
                answer = result.getContent();

            } catch (InterruptedException e) {
                log.error("An error occurred in the request for the large model， err: {}", e.getMessage());
                return Result.fail(CommonError.SERVER_ERROR.getCode(), "An error occurred in the request for the large model");
            }
            BotQAParam.QAParam conversation = new BotQAParam.QAParam();
            conversation.setUser(gson.toJson(tailLogAiAnalysisDTO.getLogs()));
            conversation.setBot(answer);
            history.add(conversation);
            QA_CACHE.put(conversationId, history);
            MilogAiConversationDO milogAiConversationDO = dao.fetch(MilogAiConversationDO.class, conversationId);
            milogAiConversationDO.setConversationContext(gson.toJson(history));
            // need to handle separately
            String originalConversation = milogAiConversationDO.getOriginalConversation();
            List<BotQAParam.QAParam> originalHistory = gson.fromJson(originalConversation, new TypeToken<List<BotQAParam.QAParam>>() {
            }.getType());
            originalHistory.add(conversation);
            milogAiConversationDO.setOriginalConversation(gson.toJson(originalHistory));
            long timestamp = System.currentTimeMillis();
            milogAiConversationDO.setUpdateTime(timestamp);
            dao.update(milogAiConversationDO);
            response.setConversationId(conversationId);
            response.setContent(answer);
            CONVERSATION_TIME.put(conversationId, timestamp);
            return Result.success(response);
        }

    }

    private List<BotQAParam.QAParam> getHistoryFromDb(Long conversationId) {
        MilogAiConversationDO milogAiConversationDO = dao.fetch(MilogAiConversationDO.class, conversationId);
        String originalConversation = milogAiConversationDO.getOriginalConversation();

        if (originalConversation == null || originalConversation.isBlank()) {
            return Collections.emptyList();
        }

        List<BotQAParam.QAParam> res = gson.fromJson(originalConversation, new TypeToken<List<BotQAParam.QAParam>>() {
        }.getType());
        return res;
    }

    @PostConstruct
    public void scheduledTask() {
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor(ThreadUtil.newNamedThreadFactory("manager-ai-conversation", false));
        scheduledExecutor.scheduleAtFixedRate(() -> SafeRun.run(this::processTask), 0, 5, TimeUnit.MINUTES);
    }

    private void processTask() {
        for (Map.Entry<Long, Long> entry : CONVERSATION_TIME.entrySet()) {
            //It has not been operated for a long time
            if (System.currentTimeMillis() - entry.getValue() > CONVERSATION_TIMEOUT) {
                saveHistory(entry.getKey());
                CONVERSATION_TIME.remove(entry.getKey());
                log.info("clean timeout conversation: {}", entry.getKey());

            }
        }
    }

    private void saveHistory(Long conversationId) {
        dao.fetch(MilogAiConversationDO.class, conversationId);
        List<BotQAParam.QAParam> history = QA_CACHE.get(conversationId);
        MilogAiConversationDO milogAiConversationDO = new MilogAiConversationDO();
        milogAiConversationDO.setUpdateTime(System.currentTimeMillis());
        milogAiConversationDO.setConversationContext(gson.toJson(history));
        milogAiConversationDO.setOriginalConversation(gson.toJson(history));
        dao.update(milogAiConversationDO);
    }

}
