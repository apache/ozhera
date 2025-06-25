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
import org.apache.ozhera.log.manager.model.bo.BotQAParam;
import org.apache.ozhera.log.manager.model.dto.LogAiAnalysisDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogAiConversationDO;
import org.apache.ozhera.log.manager.model.vo.LogAiAnalysisResponse;
import org.apache.ozhera.log.manager.service.MilogAiAnalysisService;
import org.apache.ozhera.log.manager.service.bot.ContentSimplifyBot;
import org.apache.ozhera.log.manager.service.bot.LogAnalysisBot;
import org.apache.ozhera.log.manager.user.MoneUser;
import org.nutz.dao.impl.NutDao;
import run.mone.hive.configs.LLMConfig;
import run.mone.hive.llm.LLM;
import run.mone.hive.llm.LLMProvider;
import run.mone.hive.schema.Message;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
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

    @Resource
    private ContentSimplifyBot contentSimplifyBot;

    private static final ConcurrentHashMap<Long, Map<String, List<BotQAParam.QAParam>>> QA_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Long, Map<String, List<BotQAParam.QAParam>>> REFRESH_DESK = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<Long, String> LOCK = new ConcurrentHashMap<>();
    private static final String LOCK_VALUE = "1";

    private static final String MODEL_KEY = "model";
    private static final String ORIGINAL_KEY = "original";

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
        contentSimplifyBot.setLlm(llm);
    }


    @Override
    public Result<LogAiAnalysisResponse> tailLogAiAnalysis(LogAiAnalysisDTO tailLogAiAnalysisDTO) {

        if (tailLogAiAnalysisDTO.getStoreId() == null) {
            return Result.failParam("Store id is null");
        }

        if (requestExceedLimit(tailLogAiAnalysisDTO.getLogs())){
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
                String text = formatString(param);
                analysisBot.getRc().news.put(Message.builder().content(text).build());
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

            //The first request will be created
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
            Map<String, List<BotQAParam.QAParam>> cache = new HashMap<>();
            cache.put(MODEL_KEY, history);
            cache.put(ORIGINAL_KEY, history);
            QA_CACHE.put(conversationId, cache);
            response.setConversationId(conversationId);
            response.setContent(answer);
            CONVERSATION_TIME.put(conversationId, timestamp);
            return Result.success(response);
        } else {
            String answer = "";
            conversationId = tailLogAiAnalysisDTO.getConversationId();
            //This is not first request, need lock
            LOCK.put(conversationId, LOCK_VALUE);
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
                String text = formatString(param);
                analysisBot.getRc().news.put(Message.builder().content(gson.toJson(text)).build());
                Message result = analysisBot.run().join();
                answer = result.getContent();

            } catch (InterruptedException e) {
                log.error("An error occurred in the request for the large model， err: {}", e.getMessage());
                return Result.fail(CommonError.SERVER_ERROR.getCode(), "An error occurred in the request for the large model");
            }
            BotQAParam.QAParam conversation = new BotQAParam.QAParam();
            conversation.setUser(gson.toJson(tailLogAiAnalysisDTO.getLogs()));
            conversation.setBot(answer);
            modelHistory.add(conversation);
            originalHistory.add(conversation);
            cache.put(MODEL_KEY, modelHistory);
            cache.put(ORIGINAL_KEY, originalHistory);
            QA_CACHE.put(conversationId, cache);

            LOCK.remove(conversationId);

            response.setConversationId(conversationId);
            response.setContent(answer);
            CONVERSATION_TIME.put(conversationId, System.currentTimeMillis());
            return Result.success(response);
        }

    }

    @Override
    public String testCompress(String str)  {
        String res = null;
        try {
            contentSimplifyBot.getRc().news.put(Message.builder().content(gson.toJson(str)).build());
            Message result = contentSimplifyBot.run().join();
            res = result.getContent();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("压缩前：{}, 压缩后：{}", str.length(), res.length());
        return res;
    }

    private Map<String, List<BotQAParam.QAParam>> getHistoryFromDb(Long conversationId) {
        MilogAiConversationDO milogAiConversationDO = dao.fetch(MilogAiConversationDO.class, conversationId);
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

    @PostConstruct
    public void scheduledTask() {
        scheduledExecutor = Executors.newScheduledThreadPool(2, ThreadUtil.newNamedThreadFactory("manager-ai-conversation", false));
        scheduledExecutor.scheduleAtFixedRate(() -> SafeRun.run(this::processTask), 0, 5, TimeUnit.MINUTES);
        scheduledExecutor.scheduleAtFixedRate(() -> SafeRun.run(this::checkTokenLength), 0, 1, TimeUnit.MINUTES);
    }



    private void processTask() {
        for (Map.Entry<Long, Long> entry : CONVERSATION_TIME.entrySet()) {
            //It has not been operated for a long time
            if (System.currentTimeMillis() - entry.getValue() > CONVERSATION_TIMEOUT && LOCK.get(entry.getKey()) == null) {
                saveHistory(entry.getKey());
                CONVERSATION_TIME.remove(entry.getKey());
                log.info("clean timeout conversation: {}", entry.getKey());
            }
        }
    }

    private void saveHistory(Long conversationId) {
        dao.fetch(MilogAiConversationDO.class, conversationId);
        Map<String, List<BotQAParam.QAParam>> map = QA_CACHE.get(conversationId);
        List<BotQAParam.QAParam> modelHistory = map.get(MODEL_KEY);
        List<BotQAParam.QAParam> originalHistory = map.get(ORIGINAL_KEY);
        MilogAiConversationDO milogAiConversationDO = new MilogAiConversationDO();
        milogAiConversationDO.setUpdateTime(System.currentTimeMillis());
        milogAiConversationDO.setConversationContext(gson.toJson(modelHistory));
        milogAiConversationDO.setOriginalConversation(gson.toJson(originalHistory));
        dao.update(milogAiConversationDO);
    }



    private void checkTokenLength(){
        for (Map.Entry<Long, Map<String, List<BotQAParam.QAParam>>> entry : QA_CACHE.entrySet()) {
            if (LOCK.get(entry.getKey()) != null){
                continue;
            }
            List<BotQAParam.QAParam> originalHistory = entry.getValue().get(ORIGINAL_KEY);
            Integer index = compressIndex(originalHistory);
            if (index > 0){
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
                if (!res.isBlank()){
                    List<BotQAParam.QAParam> compressedList = gson.fromJson(res, new TypeToken<List<BotQAParam.QAParam>>() {
                    }.getType());
                    compressedList.addAll(unchangeList);
                    entry.getValue().put(ORIGINAL_KEY, compressedList);
                    QA_CACHE.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private static String formatString(BotQAParam param){
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


    private static Boolean requestExceedLimit(List<String> logs){
        String request = gson.toJson(logs);
        if (request.length() >=  150000){
            return true;
        }
        return false;
    }

    private static Integer compressIndex(List<BotQAParam.QAParam> paramList){
        int index = paramList.size() - 1;
        int limit = 200000;
        int sum = 0;
        for (int i = paramList.size() - 1; i >= 0; i--) {
            BotQAParam.QAParam param = paramList.get(i);
            String str = gson.toJson(param);
            sum += str.length();
            index = i;
            if (sum >= limit || paramList.size() - i >= 5){
                break;
            }
        }
        return index;
    }


}
