package org.apache.ozhera.log.api.model.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpaceInfo implements Serializable {
    private Long spaceId;

    private String spaceName;

    private String spaceDescription;

    private UserInfo userInfo;
}
