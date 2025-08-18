package org.apache.ozhera.log.manager.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class LogAiAnalysisDTO {
    private List<String> logs;

    private Long storeId;

    private Long conversationId;
}
