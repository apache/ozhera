package com.xiaomi.mone.log.manager.model.bo;

import com.xiaomi.mone.log.manager.model.Pair;
import lombok.Data;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/12 10:39
 */
@Data
public class IpPartitionBalance {

    private List<Pair<Long, String>> spaceList;

}
