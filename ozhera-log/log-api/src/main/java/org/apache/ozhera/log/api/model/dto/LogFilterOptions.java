package org.apache.ozhera.log.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/3/6 16:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogFilterOptions implements Serializable {
    private Long projectId;
    private Long envId;
    private String traceId;
    private String level;
    private String startTime;
    private String endTime;
}
