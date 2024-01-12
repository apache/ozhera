package com.xiaomi.mone.log.manager.model.vo;

import com.xiaomi.mone.log.manager.model.PageVo;
import lombok.Data;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/16 14:14
 */
@Data
public class SpaceIpParam extends PageVo {
    private String machineRoom;
    private String uniqueKey;
    private Long spaceId;
    private String spaceName;

    private List<Long> spaceIds;
    private List<String> uniqueKeys;
}
