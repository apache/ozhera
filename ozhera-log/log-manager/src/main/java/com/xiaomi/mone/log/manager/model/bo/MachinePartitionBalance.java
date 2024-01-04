package com.xiaomi.mone.log.manager.model.bo;

import cn.hutool.core.lang.Pair;
import lombok.Data;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/9/19 14:49
 */
@Data
public class MachinePartitionBalance {

    private String machineUnique;
    private List<Pair<Long, String>> data;

}
