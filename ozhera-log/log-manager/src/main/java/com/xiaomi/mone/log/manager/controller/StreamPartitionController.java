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
package com.xiaomi.mone.log.manager.controller;

import com.xiaomi.mone.log.common.Result;
import com.xiaomi.mone.log.manager.model.Pair;
import com.xiaomi.mone.log.manager.model.bo.SpacePartitionBalance;
import com.xiaomi.mone.log.manager.model.page.PageInfo;
import com.xiaomi.mone.log.manager.model.vo.MachinePartitionParam;
import com.xiaomi.mone.log.manager.model.vo.SpaceIpParam;
import com.xiaomi.mone.log.manager.service.StreamPartitionService;
import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author wtt
 * @version 1.0
 * @description Consumption machine configuration, this menu can only be used by administrators
 * @date 2023/9/19 14:43
 */
@Controller
public class StreamPartitionController {

    @Resource
    private StreamPartitionService streamPartitionService;

    @RequestMapping(path = "/stream/space/stream/key/list")
    public Result<PageInfo<Pair<String, String>>> queryStreamList(MachinePartitionParam partitionParam) {
        if (StringUtils.isEmpty(partitionParam.getMachineRoom())) {
            return Result.failParam("machineRoom 不能为空");
        }
        return Result.success(streamPartitionService.queryStreamList(partitionParam));
    }

    /**
     * Query the stream list under unique unique
     *
     * @param partitionParam
     * @return
     */
    @RequestMapping(path = "/stream/key/partition/list")
    public Result<PageInfo<Pair<Long, String>>> queryIpPartitionBalance(MachinePartitionParam partitionParam) {
        if (StringUtils.isEmpty(partitionParam.getMachineRoom())) {
            return Result.failParam("machineRoom 不能为空");
        }
        if (StringUtils.isEmpty(partitionParam.getUniqueKey())) {
            return Result.failParam("uniqueKey 不能为空");
        }
        return Result.success(streamPartitionService.queryIpPartitionBalance(partitionParam));
    }

    /**
     * delete configuration
     *
     * @param param
     * @return
     */
    @RequestMapping(path = "/stream/key/space/delete")
    public Result<?> delSpaceToIp(SpaceIpParam param) {
        if (null == param || StringUtils.isEmpty(param.getUniqueKey()) || Objects.isNull(param.getSpaceId())) {
            return Result.failParam("参数是不能为空");
        }
        return Result.success(streamPartitionService.delSpaceToIp(param));
    }

    /**
     * New
     *
     * @param param
     * @return
     */
    @RequestMapping(path = "/stream/key/space/add")
    public Result<?> addSpaceToIp(SpaceIpParam param) {
        if (null == param || CollectionUtils.isEmpty(param.getSpaceIds()) ||
                CollectionUtils.isEmpty(param.getUniqueKeys())) {
            return Result.failParam("参数是不能为空");
        }
        return Result.success(streamPartitionService.addSpaceToIp(param));
    }

    @RequestMapping(path = "/stream/key/space/list")
    public Result<List<Pair<String, Long>>> findUnIncludedSpaceList(SpaceIpParam param) {
        if (null == param || StringUtils.isEmpty(param.getMachineRoom())) {
            return Result.failParam("machineRoom 不能为空");
        }
        if (StringUtils.isEmpty(param.getUniqueKey())) {
            return Result.failParam("参数uniqueKey是不能为空");
        }
        return Result.success(streamPartitionService.findUnIncludedSpaceList(param));
    }

    /**
     * space dimension query
     *
     * @param partitionParam
     * @return
     */
    @RequestMapping(path = "/stream/space/list")
    public Result<PageInfo<SpacePartitionBalance>> querySpacePartitionBalance(MachinePartitionParam partitionParam) {
        if (StringUtils.isEmpty(partitionParam.getMachineRoom())) {
            return Result.failParam("machineRoom 不能为空");
        }
        return Result.success(streamPartitionService.querySpacePartitionBalance(partitionParam));
    }

    @RequestMapping(path = "/stream/space/key/list")
    public Result<List<Pair<String, String>>> queryAllUniqueKeyList(SpaceIpParam param) {
        if (null == param || StringUtils.isEmpty(param.getMachineRoom())) {
            return Result.failParam("machineRoom 不能为空");
        }
        if (Objects.isNull(param.getSpaceId())) {
            return Result.failParam("spaceId 不能为空");
        }
        return Result.success(streamPartitionService.queryAllUniqueKeyList(param));
    }
}
