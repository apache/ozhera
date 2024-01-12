package com.xiaomi.mone.log.manager.model.bo;

import lombok.Data;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/9/19 14:49
 */
@Data
public class SpacePartitionBalance {
    private Long spaceId;
    private String spaceName;
    private String spaceIdentifiers;
    private List<String> machineUniques;

}
