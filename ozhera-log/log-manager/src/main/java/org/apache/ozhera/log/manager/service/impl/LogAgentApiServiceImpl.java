package org.apache.ozhera.log.manager.service.impl;


import com.google.gson.Gson;
import com.xiaomi.youpin.docean.plugin.dubbo.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.api.model.dto.LogAgentBaseDTO;
import org.apache.ozhera.log.api.service.HeraLogApiService;
import org.apache.ozhera.log.api.service.LogAgentApiService;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.exception.CommonError;
import org.apache.ozhera.log.manager.model.MilogSpaceParam;
import org.apache.ozhera.log.manager.model.dto.MilogSpaceDTO;
import org.apache.ozhera.log.manager.user.MoneUser;

import javax.annotation.Resource;

@Slf4j
@Service(interfaceClass = HeraLogApiService.class, group = "$dubbo.group", timeout = 10000)
public class LogAgentApiServiceImpl implements LogAgentApiService {

    private static final String SUCCESS = "success";

    @Resource
    private LogSpaceServiceImpl logSpaceService;

    private static final Gson GSON = new Gson();

    @Override
    public String createSpace(LogAgentBaseDTO dto) {
        if (dto.getSpaceInfo().getSpaceName() == null || dto.getSpaceInfo().getSpaceName().isEmpty()) {
            return "The space name cannot be empty!";
        }
        if (dto.getSpaceInfo().getSpaceDescription() == null || dto.getSpaceInfo().getSpaceDescription().isEmpty()) {
            return "The space description cannot be empty!";
        }
        if (dto.getUserInfo().getUser() == null || dto.getUserInfo().getUser().isEmpty()) {
            return "The user information is empty. Please provide the correct user information!";
        }


        MilogSpaceParam param = new MilogSpaceParam();
        param.setSpaceName(dto.getSpaceInfo().getSpaceName());
        param.setDescription(dto.getSpaceInfo().getSpaceDescription());
        try {
            Result<String> result = logSpaceService.newMilogSpace(param, dto.getUserInfo().getUser());
            if (result.getCode() != CommonError.Success.getCode()) {
                return "Create space failed, the reason for the failure is " + result.getMessage();
            }
        } catch (Exception e) {
            log.error("create space failed, name: {}, description: {}, error msg: {}", dto.getSpaceInfo().getSpaceName(), dto.getSpaceInfo().getSpaceDescription(), e.getMessage());
            return "Create space failed, the reason for the failure is " + e.getMessage();
        }

        return SUCCESS;
    }

    @Override
    public String updateSpace(LogAgentBaseDTO dto) {
        if (dto.getSpaceInfo().getSpaceId() == null || dto.getSpaceInfo().getSpaceId() < 0) {
            return "The space id cannot be empty!";
        }

        if (dto.getSpaceInfo().getSpaceName() == null || dto.getSpaceInfo().getSpaceName().isEmpty()) {
            return "The space name cannot be empty!";
        }
        if (dto.getSpaceInfo().getSpaceDescription() == null || dto.getSpaceInfo().getSpaceDescription().isEmpty()) {
            return "The space description cannot be empty!";
        }
        if (dto.getUserInfo().getUser() == null || dto.getUserInfo().getUser().isEmpty() || dto.getUserInfo().getUserType() == null) {
            return "The user information is empty. Please provide the correct user information!";
        }


        MilogSpaceParam param = new MilogSpaceParam();
        param.setId(dto.getSpaceInfo().getSpaceId());
        param.setSpaceName(dto.getSpaceInfo().getSpaceName());
        param.setDescription(dto.getSpaceInfo().getSpaceDescription());

        MoneUser currentUser = MoneUser.builder()
                .user(dto.getUserInfo().getUser())
                .zone(dto.getUserInfo().getZone())
                .userType(dto.getUserInfo().getUserType())
                .build();
        try {
            Result<String> result = logSpaceService.updateMilogSpace(param, currentUser);
            if (result.getCode() != CommonError.Success.getCode()) {
                return "Update space failed, the reason for the failure is " + result.getMessage();
            }
        } catch (Exception e) {
            log.error("Update space failed, name: {}, description: {}, error msg: {}", dto.getSpaceInfo().getSpaceName(), dto.getSpaceInfo().getSpaceDescription(), e.getMessage());
            return "Update space failed, the reason for the failure is " + e.getMessage();
        }

        return SUCCESS;
    }

    @Override
    public String deleteSpace(LogAgentBaseDTO dto) {

        if (dto.getSpaceInfo().getSpaceId() == null || dto.getSpaceInfo().getSpaceId() < 0) {
            return "The space id cannot be empty!";
        }
        if (dto.getUserInfo().getUser() == null || dto.getUserInfo().getUser().isEmpty() || dto.getUserInfo().getUserType() == null) {
            return "The user information is empty. Please provide the correct user information!";
        }

        MoneUser currentUser = MoneUser.builder()
                .user(dto.getUserInfo().getUser())
                .userType(dto.getUserInfo().getUserType())
                .build();
        try {
            Result<String> result = logSpaceService.deleteMilogSpace(dto.getSpaceInfo().getSpaceId(), currentUser);
            if (result.getCode() != CommonError.Success.getCode()) {
                return "Delete space failed, the reason for the failure is " + result.getMessage();
            }
        } catch (Exception e) {
            log.error("Delete space failed, space id: {}, error msg: {}", dto.getSpaceInfo().getSpaceId(), e.getMessage());
            return "Delete space failed, the reason for the failure is " + e.getMessage();
        }

        return SUCCESS;
    }

    @Override
    public String getSpace(Long spaceId) {
        try{
            Result<MilogSpaceDTO> result = logSpaceService.getMilogSpaceById(spaceId);
            if(result.getCode() != CommonError.Success.getCode()) {
                return "Get space failed, the reason for the failure is " + result.getMessage();
            }
            return GSON.toJson(result.getData());
        } catch (Exception e) {
            log.error("Get space failed! space id: {}, message: {}", spaceId, e.getMessage());
            return "Get space failed, the reason for the failure is " + e.getMessage();
        }
    }
}
