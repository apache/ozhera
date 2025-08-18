/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ozhera.log.manager.service.impl;

import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.ozhera.log.api.model.meta.NodeCollInfo;
import org.apache.ozhera.log.api.model.vo.TailLogProcessDTO;
import org.apache.ozhera.log.api.model.vo.UpdateLogProcessCmd;
import org.apache.ozhera.log.api.service.LogProcessService;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.domain.LogProcess;
import org.apache.ozhera.log.manager.mapper.MilogLogProcessMapper;
import org.apache.ozhera.log.manager.model.pojo.MilogLogProcessDOMybatis;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LogProcessServiceImpl implements LogProcessService {

    @Resource
    private MilogLogProcessMapper processMapper;

    @Resource
    private LogProcess logProcess;

    @Resource
    private MilogLogTailDao logtailDao;

    /**
     * Update log collection progress
     *
     * @param cmd
     */
    @Override
    public void updateLogProcess(UpdateLogProcessCmd cmd) {
        if (null != logProcess) {
            logProcess.updateLogProcess(cmd);
        }
    }

    @Override
    public List<String> getAllIps() {
        return logProcess.getAllAgentList();
    }

    @Override
    public NodeCollInfo getNodeCollInfo(String ip) {
        return logProcess.getNodeCollInfo(ip);
    }

    public MilogLogProcessDOMybatis getByIdFramework(Long id) {
        return processMapper.selectById(id);
    }

    /**
     * Get the log collection progress of the store
     *
     * @param type
     * @param value
     * @return
     */
    public Result<List<TailLogProcessDTO>> getStoreLogProcess(String type, String value) {
        if (StringUtils.isEmpty(type) || StringUtils.isEmpty(value)) {
            return Result.failParam("Type and value cannot be empty");
        }
        List<TailLogProcessDTO> dtoList;
        switch (type) {
            case "store":
                dtoList = logProcess.getStoreLogProcess(Long.parseLong(value), "");
                break;
            case "tail":
                dtoList = logProcess.getTailLogProcess(Long.parseLong(value), "");
                break;
            case "ip":
                String[] params = value.split(",");
                dtoList = logProcess.getStoreLogProcess(Long.parseLong(params[0]), params[1]);
                break;
            case "tail&ip":
                String[] params2 = value.split(",");
                dtoList = logProcess.getTailLogProcess(Long.parseLong(params2[0]), params2[1]);
                break;
            default:
                return Result.failParam("The type type is not legal");
        }
        return Result.success(dtoList);
    }

    public Result<List<UpdateLogProcessCmd.CollectDetail>> getColProcessImperfect(Double progressRation) {
        if (null == progressRation) {
            return Result.failParam("The parameter cannot be empty");
        }
        return Result.success(upColProcessTailName(logProcess.getColProcessImperfect(progressRation)));
    }

    @Nullable
    private List<UpdateLogProcessCmd.CollectDetail> upColProcessTailName(List<UpdateLogProcessCmd.CollectDetail> colProcessImperfect) {
        // Create a mapping of tailId to CollectDetail
        Map<String, UpdateLogProcessCmd.CollectDetail> tailIdToDetailMap = new HashMap<>();

        // Process and update details
        colProcessImperfect.parallelStream().forEach(collectDetail -> {
            String tailId = collectDetail.getTailId();
            if (StringUtils.isNotBlank(tailId)) {
                try {
                    MilogLogTailDo logTailDo = logtailDao.queryById(Long.valueOf(tailId));
                    if (logTailDo != null) {
                        collectDetail.setTailName(logTailDo.getTail());
                    }

                    // Update the map with the latest details
                    tailIdToDetailMap.merge(tailId, collectDetail, (existingDetail, newDetail) -> {
                        existingDetail.getIpList().addAll(newDetail.getIpList());
                        existingDetail.getIpList().stream().distinct().collect(Collectors.toList());
                        existingDetail.getFileProgressDetails().addAll(newDetail.getFileProgressDetails());
                        return existingDetail;
                    });
                } catch (Exception e) {
                    log.error("process data error", e);
                }
            }
        });

        // Return the values from the map as a list
        return new ArrayList<>(tailIdToDetailMap.values());

    }
}
