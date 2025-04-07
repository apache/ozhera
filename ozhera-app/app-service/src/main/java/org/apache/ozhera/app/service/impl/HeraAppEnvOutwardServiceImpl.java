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
package org.apache.ozhera.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import org.apache.ozhera.app.api.model.HeraAppEnvData;
import org.apache.ozhera.app.api.model.HeraSimpleEnv;
import org.apache.ozhera.app.api.service.HeraAppEnvOutwardService;
import org.apache.ozhera.app.dao.mapper.HeraAppEnvMapper;
import org.apache.ozhera.app.model.HeraAppEnv;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service(interfaceClass = HeraAppEnvOutwardService.class, group = "${dubbo.group}")
@org.springframework.stereotype.Service
public class HeraAppEnvOutwardServiceImpl implements HeraAppEnvOutwardService {

    private final HeraAppEnvMapper heraAppEnvMapper;

    public HeraAppEnvOutwardServiceImpl(HeraAppEnvMapper heraAppEnvMapper) {
        this.heraAppEnvMapper = heraAppEnvMapper;
    }

    @Override
    public List<HeraSimpleEnv> querySimpleEnvAppBaseInfoId(Integer id) {
        QueryWrapper<HeraAppEnv> queryWrapper = new QueryWrapper<HeraAppEnv>().eq("hera_app_id", id);
        List<HeraAppEnv> heraAppEnvs = heraAppEnvMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(heraAppEnvs)) {
            return heraAppEnvs.stream().map(HeraAppEnv::toHeraSimpleEnv).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public List<HeraAppEnvData> queryEnvById(Long id, Long heraAppId, Long envId) {
        LambdaQueryWrapper<HeraAppEnv> queryWrapper = new LambdaQueryWrapper<>();
        if (null != id) {
            queryWrapper.eq(HeraAppEnv::getId, id);
        }
        if (null != heraAppId) {
            queryWrapper.eq(HeraAppEnv::getHeraAppId, heraAppId);
        }
        if (null != envId) {
            queryWrapper.eq(HeraAppEnv::getEnvId, envId);
        }
        List<HeraAppEnv> heraAppEnvs = heraAppEnvMapper.selectList(queryWrapper);
        if (CollectionUtils.isNotEmpty(heraAppEnvs)) {
            return heraAppEnvs.stream().map(data -> {
                HeraAppEnvData heraAppEnvData = new HeraAppEnvData();
                BeanUtils.copyProperties(data, heraAppEnvData);
                return heraAppEnvData;
            }).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }
}
