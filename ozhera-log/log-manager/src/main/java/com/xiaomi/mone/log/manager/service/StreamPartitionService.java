/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.mone.log.manager.service;

import cn.hutool.core.util.PageUtil;
import com.xiaomi.mone.log.manager.model.PageVo;
import com.xiaomi.mone.log.manager.model.Pair;
import com.xiaomi.mone.log.manager.model.bo.SpacePartitionBalance;
import com.xiaomi.mone.log.manager.model.page.PageInfo;
import com.xiaomi.mone.log.manager.model.vo.MachinePartitionParam;
import com.xiaomi.mone.log.manager.model.vo.SpaceIpParam;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/9/19 15:05
 */
public interface StreamPartitionService {
    PageInfo<SpacePartitionBalance> querySpacePartitionBalance(MachinePartitionParam partitionParam);

    PageInfo<Pair<Long, String>> queryIpPartitionBalance(MachinePartitionParam partitionParam);

    Boolean addSpaceToIp(SpaceIpParam param);

    Boolean delSpaceToIp(SpaceIpParam param);

    PageInfo<Pair<String, String>> queryStreamList(MachinePartitionParam partitionParam);

    boolean streamReBalance();

    String queryStreamHostname(String ip);

    default <T> PageInfo<T> buildPageInfo(PageVo pageVo, List<T> dataList, List<T> pageList) {
        PageInfo<T> pageInfo = new PageInfo<>();
        pageInfo.setPage(pageVo.getPageNum());
        pageInfo.setPageSize(pageVo.getPageSize());
        pageInfo.setTotal(dataList.size());
        pageInfo.setTotalPageCount(PageUtil.totalPage(dataList.size(), pageVo.getPageSize()));
        pageInfo.setList(pageList);
        return pageInfo;
    }

    List<Pair<String, Long>> findUnIncludedSpaceList(SpaceIpParam param);

    List<Pair<String, String>> queryAllUniqueKeyList(SpaceIpParam param);
}
