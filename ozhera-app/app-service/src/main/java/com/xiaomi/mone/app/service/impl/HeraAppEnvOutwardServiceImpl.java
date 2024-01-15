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
package com.xiaomi.mone.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.xiaomi.mone.app.api.model.HeraSimpleEnv;
import com.xiaomi.mone.app.api.service.HeraAppEnvOutwardService;
import com.xiaomi.mone.app.dao.mapper.HeraAppEnvMapper;
import com.xiaomi.mone.app.model.HeraAppEnv;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.dubbo.config.annotation.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/11/12 11:39
 */
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
}
