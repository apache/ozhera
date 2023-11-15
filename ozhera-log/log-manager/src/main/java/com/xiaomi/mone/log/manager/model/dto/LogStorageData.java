package com.xiaomi.mone.log.manager.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/10 16:02
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogStorageData {

    private Long clusterId;

    private Long storeId;

    private String logStoreName;

    private String updateStoreName;

    private String keys;

    private Integer logType;

    private String columnTypes;

    private String updateKeys;

    private String updateColumnTypes;
}
