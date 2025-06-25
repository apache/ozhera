package org.apache.ozhera.log.manager.service.bot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.xiaomi.youpin.docean.anno.Service;
import run.mone.hive.Environment;
import run.mone.hive.llm.LLM;
import run.mone.hive.llm.LLMProvider;
import run.mone.hive.roles.Role;
import run.mone.hive.schema.AiMessage;
import run.mone.hive.schema.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ContentSimplifyBot extends Role {
    private String baseText = """
            你是一个对话摘要压缩助手，接下来我会提供一段对话历史，格式是一个 JSON 列表，列表中的每一项是一个对象，包含三个字段：
            
            - "time"：会话开始的时间
            - "user"：表示用户的提问或陈述
            - "bot"：表示机器人的回答
            
            你的任务是对整段对话进行语义压缩，保留关键信息，删除冗余、重复、细节性描述，使对话更加简洁、精炼。但请务必：
            
            1. **保持输出数据结构与输入一致**：输出仍然是一个 JSON 列表，每一项仍然包含 "time" 、"user" 和 "bot" 三个字段。
            2. **尽可能减少轮数**：若多轮对话围绕同一问题展开，可以合并为一轮，但必须保留语义完整，并且时间选择为多轮中最后一轮的时间。
            3. **对于一些无关的信息，或者没有什么用的信息直接去除，一定在保留核心关键信息的情况下尽可能的压缩，至少保证压缩后的字符为压缩前的30%往下
            4. **如果每轮的数据中存在原始的日志信息，那么对于日志信息不要进行压缩，需要保持原样
            5. **不得添加任何非对话内容**，例如“压缩后的内容如下”、“总结如下”等。
            6. **输出必须是一个合法的 JSON 列表，结构和字段不变**。
            
            下面是原始对话历史，请进行压缩（注意格式）：
            {{original_text}}
            """;

    public ContentSimplifyBot() {
        super("ContentSimplifyBot", "压缩历史对话");
        setEnvironment(new Environment());
    }

    @Override
    public CompletableFuture<Message> run() {
        Message msg = this.rc.getNews().poll();
        String content = msg.getContent();
        String text = baseText.replace("{{original_text}}", content);
        JsonObject req = getReq(llm, text);
        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.builder().jsonContent(req).build());
        String result = llm.syncChat(this, messages);

        return CompletableFuture.completedFuture(Message.builder().content(result).build());
    }

    private JsonObject getReq(LLM llm, String text) {
        JsonObject req = new JsonObject();
        if (llm.getConfig().getLlmProvider() == LLMProvider.CLAUDE_COMPANY) {
            req.addProperty("role", "user");
            JsonArray contentJsons = new JsonArray();
            JsonObject obj1 = new JsonObject();
            obj1.addProperty("type", "text");
            obj1.addProperty("text", text);
            contentJsons.add(obj1);
            req.add("content", contentJsons);
        }
        return req;
    }
}
