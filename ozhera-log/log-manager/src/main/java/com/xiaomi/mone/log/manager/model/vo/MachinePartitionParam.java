package com.xiaomi.mone.log.manager.model.vo;

import com.xiaomi.mone.log.manager.model.PageVo;
import lombok.Data;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/9/19 15:44
 */
@Data
public class MachinePartitionParam extends PageVo {
    private String machineRoom;
    private String spaceName;
    private String uniqueKey;
}
