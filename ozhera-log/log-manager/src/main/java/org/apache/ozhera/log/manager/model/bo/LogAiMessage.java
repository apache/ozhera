package org.apache.ozhera.log.manager.model.bo;

import lombok.Data;

@Data
public class LogAiMessage {

    public enum Role{
        system,
        user,
        assistant,
    }

    private Role role;
    private String content;

    public static LogAiMessage user(String content) {
        LogAiMessage aiMessage = new LogAiMessage();
        aiMessage.setRole(Role.user);
        aiMessage.setContent(content);
        return aiMessage;
    }

    public static LogAiMessage system(String content) {
        LogAiMessage aiMessage = new LogAiMessage();
        aiMessage.setRole(Role.system);
        aiMessage.setContent(content);
        return aiMessage;
    }

    public static LogAiMessage assistant(String content) {
        LogAiMessage aiMessage = new LogAiMessage();
        aiMessage.setRole(Role.assistant);
        aiMessage.setContent(content);
        return aiMessage;
    }
}
