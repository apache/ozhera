package org.apache.ozhera.log.api.service;

import org.apache.ozhera.log.api.model.dto.LogAgentBaseDTO;

public interface LogAgentApiService {

    String createSpace(LogAgentBaseDTO dto);

    String updateSpace(LogAgentBaseDTO dto);

    String deleteSpace(LogAgentBaseDTO dto);

    String getSpace(Long spaceId);


}
