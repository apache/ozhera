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
package com.xiaomi.hera.trace.etl.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.xiaomi.hera.trace.etl.api.service.TraceEtlService;
import com.xiaomi.hera.trace.etl.domain.HeraTraceConfigVo;
import com.xiaomi.hera.trace.etl.domain.HeraTraceEtlConfig;
import com.xiaomi.hera.trace.etl.domain.PageData;
import com.xiaomi.hera.trace.etl.mapper.HeraTraceEtlConfigMapper;
import com.xiaomi.hera.trace.etl.util.pool.AsyncNotify;
import com.xiaomi.youpin.infra.rpc.Result;
import com.xiaomi.youpin.infra.rpc.errors.GeneralCodes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * @Description Initialize through the bootstrap project BeanConfig to avoid unwanted project startup errors
 * @Author dingtao
 * @Date 2022/4/18 3:31 下午
 */
@Slf4j
public class ManagerService {

    @Autowired
    private AsyncNotify asyncNotify;

    private TraceEtlService traceEtlService;

    private HeraTraceEtlConfigMapper heraTraceEtlConfigMapper;

    public ManagerService(HeraTraceEtlConfigMapper heraTraceEtlConfigMapper) {
        this.heraTraceEtlConfigMapper = heraTraceEtlConfigMapper;
    }

    public ManagerService(HeraTraceEtlConfigMapper heraTraceEtlConfigMapper, TraceEtlService traceEtlService) {
        this.heraTraceEtlConfigMapper = heraTraceEtlConfigMapper;
        this.traceEtlService = traceEtlService;
    }

    public List<HeraTraceEtlConfig> getAll(HeraTraceConfigVo vo) {
        QueryWrapper<HeraTraceEtlConfig> qw = new QueryWrapper();
        qw.eq("status", "1");
        if (vo.getBindId() != null) {
            qw.eq("bind_id", vo.getBindId());
        }
        if (StringUtils.isNotEmpty(vo.getAppName())) {
            qw.eq("app_name", vo.getAppName());
        }
        return heraTraceEtlConfigMapper.selectList(qw);
    }

    public PageData<List<HeraTraceEtlConfig>> getAllPage(HeraTraceConfigVo vo) {
        PageData<List<HeraTraceEtlConfig>> pageData = new PageData<>();
        pageData.setPage(vo.getPage());
        pageData.setPageSize(vo.getPageSize());
        PageHelper.startPage(vo.getPage(), vo.getPageSize());
        Page<HeraTraceEtlConfig> all = heraTraceEtlConfigMapper.getAllPage(vo.getUser());
        PageInfo<HeraTraceEtlConfig> heraTraceEtlConfigPageInfo = new PageInfo<>(all);
        pageData.setTotal(heraTraceEtlConfigPageInfo.getTotal());
        pageData.setPages(heraTraceEtlConfigPageInfo.getPages());
        pageData.setList(heraTraceEtlConfigPageInfo.getList());
        return pageData;
    }

    public HeraTraceEtlConfig getByBaseInfoId(Integer baseInfoId) {
        return heraTraceEtlConfigMapper.getByBaseInfoId(baseInfoId);
    }

    public HeraTraceEtlConfig getById(Integer id) {
        return heraTraceEtlConfigMapper.selectById(id);
    }

    public Result insertOrUpdate(HeraTraceEtlConfig config, String user) {
        Date now = new Date();
        int i = 0;
        if (config.getId() == null) {
            // Check for existence
            HeraTraceEtlConfig byBaseInfoId = heraTraceEtlConfigMapper.getByBaseInfoId(config.getBaseInfoId());
            if(byBaseInfoId != null){
                return Result.fail(GeneralCodes.InternalError, "The item configuration already exists. Do not add it again");
            }
            config.setCreateTime(now);
            config.setUpdateTime(now);
            config.setCreateUser(user);
            i = heraTraceEtlConfigMapper.insert(config);
            if (i > 0) {
                asyncNotify.submit(() -> {
                    try {
                        traceEtlService.insertConfig(config);
                    } catch (Exception e) {
                        log.error("insert sync etl error : ", e);
                    }
                });
            }
        } else {
            config.setUpdateTime(now);
            config.setUpdateUser(user);
            i = heraTraceEtlConfigMapper.updateById(config);
            if (i > 0) {
                asyncNotify.submit(() -> {
                    try {
                        traceEtlService.updateConfig(config);
                    } catch (Exception e) {
                        log.error("update sync etl error : ", e);
                    }
                });
            }
        }
        return i > 0 ? Result.success(null) : Result.fail(GeneralCodes.InternalError, "Operation failure");
    }

    public int delete(HeraTraceEtlConfig config) {
        HeraTraceEtlConfig heraTraceEtlConfig = heraTraceEtlConfigMapper.selectById(config.getId());
        int i = heraTraceEtlConfigMapper.deleteById(config.getId());
        if (i > 0) {
            asyncNotify.submit(() -> {
                try {
                    traceEtlService.deleteConfig(heraTraceEtlConfig);
                } catch (Exception e) {
                    log.error("delete sync etl error : ", e);
                }
            });
        }
        return i;
    }
}
