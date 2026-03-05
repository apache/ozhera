package org.apache.ozhera.log.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogAgentBaseDTO implements Serializable {

    private SpaceInfo spaceInfo = new SpaceInfo();

    private UserInfo userInfo = new UserInfo();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static  class SpaceInfo implements Serializable {
        private Long spaceId;

        private String spaceName;

        private String spaceDescription;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static  class UserInfo implements Serializable{
        private String user;

        private Integer userType;

        private String zone;
    }
}
