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

package org.apache.ozhera.monitor.service.impl;

import org.apache.ozhera.monitor.dao.GrafanaBlackListDao;
import org.apache.ozhera.monitor.dao.model.AppGrafanaBlackList;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.GrafanaBlackListService;
import org.apache.ozhera.monitor.service.model.PageData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class GrafanaBlackListServiceImpl implements GrafanaBlackListService {
    
    @Autowired
    GrafanaBlackListDao grafanaBlackListDao;
    
    @Override
    public Result createBlackList(String serverName) {
        //插库
        List<AppGrafanaBlackList> blackListByServerName = grafanaBlackListDao.getBlackListByServerName(serverName);
        if (blackListByServerName.size() >= 1) {
            return Result.fail(ErrorCode.REPEAT_ADD_PROJECT);
        }
        Integer blackList = grafanaBlackListDao.createBlackList(serverName);
        if (blackList == null || blackList == -1 || blackList == 0) {
            log.error("GrafanaBlackListService.createBlackList error");
            return Result.fail(ErrorCode.unknownError);
        }
        return Result.success(blackList);
    }
    
    @Override
    public Result getBlackList(String serverName) {
        //查库
        AppGrafanaBlackList blackList = grafanaBlackListDao.getBlackList(serverName);
        return Result.success(blackList);
    }
    
    @Override
    public Result delBlackList(String serverName) {
        //删除
        Integer res = grafanaBlackListDao.delBlackListByServerName(serverName);
        if (res == null || res == 0 || res == -1) {
            log.error("GrafanaBlackListService.delBlackList error");
            return Result.fail(ErrorCode.unknownError);
        }
        return Result.success(res);
    }
    
    @Override
    public Result getBlackListList(Integer page, Integer pageSize) {
        //获取列表
        PageData pd = new PageData();
        pd.setPage(page);
        pd.setPageSize(pageSize);
        pd.setTotal(grafanaBlackListDao.getTotalBlackList());
        pd.setList(grafanaBlackListDao.getAllBlackList(page, pageSize));
        return Result.success(pd);
    }
    
    @Override
    public boolean isInBlackList(String serverName) {
        List<AppGrafanaBlackList> blackListByServerName = grafanaBlackListDao.getBlackListByServerName(serverName);
        if (blackListByServerName.size() >= 1) {
            return true;
        }
        return false;
    }
    
    
}
