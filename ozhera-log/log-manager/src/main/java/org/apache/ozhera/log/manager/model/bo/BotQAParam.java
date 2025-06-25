package org.apache.ozhera.log.manager.model.bo;

import lombok.Data;

import java.util.List;

@Data
public class BotQAParam {
    private List<QAParam> historyConversation;
    private String LatestQuestion;

    @Data
    public static class QAParam {
        private String bot;
        private String user;
        private String time;
    }
}
