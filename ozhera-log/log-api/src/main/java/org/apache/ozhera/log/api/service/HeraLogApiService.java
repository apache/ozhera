package org.apache.ozhera.log.api.service;

import org.apache.ozhera.log.api.model.dto.LogFilterOptions;
import org.apache.ozhera.log.api.model.dto.LogUrlParam;

import java.util.List;
import java.util.Map;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/3/6 16:12
 */
public interface HeraLogApiService {

    List<String> queryLogUrl(LogUrlParam logUrlParam);

    List<Map<String, Object>> queryLogData(LogFilterOptions filterOptions);

}
