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

import org.apache.ozhera.monitor.dao.AppQualityMarketDao;
import org.apache.ozhera.monitor.dao.model.AppQualityMarket;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.QualityMarketService;
import org.apache.ozhera.monitor.service.model.PageData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QualityMarketServiceImpl implements QualityMarketService {
    
    @Autowired
    AppQualityMarketDao appQualityMarketDao;
    
    @Override
    public Result createMarket(String user, String marketName, String serviceList, String remark) {
        try {
            //按;切分serviceList
            String[] services = serviceList.split(";");
            //入库
            AppQualityMarket appQualityMarket = new AppQualityMarket();
            appQualityMarket.setMarketName(marketName.trim());
            appQualityMarket.setServiceList(serviceList.trim());
            appQualityMarket.setCreator(user);
            appQualityMarket.setLastUpdater(user);
            appQualityMarket.setRemark(remark.trim());
            int dbResult = appQualityMarketDao.insertServiceMarket(appQualityMarket);
            log.info("QualityMarketService.createMarket dbResult: {}", dbResult);
            return Result.success("success");
        } catch (Exception e) {
            log.error("QualityMarketService.createMarket error : {}", e.toString());
            return Result.fail(ErrorCode.unknownError);
        }
    }
    @Override
    public Result searchMarket(String user, int id) {
        try {
            AppQualityMarket appQualityMarket = appQualityMarketDao.SearchAppQualityMarket(id);
            return Result.success(appQualityMarket);
        } catch (Exception e) {
            log.error("QualityMarketService.searchMarket error : {}", e.toString());
            return Result.fail(ErrorCode.unknownError);
        }
    }
    
    //改
    @Override
    public Result updateMarket(String user, int id, String serviceList, String marketName, String remark) {
        try {
            //查库是否有该记录
            AppQualityMarket appQualityMarket = appQualityMarketDao.SearchAppQualityMarket(id);
            if (appQualityMarket == null) {
                return Result.fail(ErrorCode.nonExistentServiceMarketId);
            }
            appQualityMarket.setMarketName(marketName.trim());
            appQualityMarket.setServiceList(serviceList.trim());
            appQualityMarket.setLastUpdater(user);
            appQualityMarket.setRemark(remark.trim());
            int dbResult = appQualityMarketDao.updateQualityMarket(appQualityMarket);
            log.info("QualityMarketService.updateMarket dbResult: {}", dbResult);
            return Result.success("success");
        } catch (Exception e) {
            log.error("QualityMarketService.updateMarket error : {}", e.toString());
            return Result.fail(ErrorCode.unknownError);
        }
    }
    
    //删
    @Override
    public Result deleteMarket(String user, Integer id) {
        try {
            //查库是否有该记录
            AppQualityMarket appQualityMarket = appQualityMarketDao.SearchAppQualityMarket(id);
            if (appQualityMarket == null) {
                return Result.fail(ErrorCode.nonExistentServiceMarketId);
            }
            //删除
            int result = appQualityMarketDao.deleteQualityMarket(id);
            log.info("QualityMarketService.deleteMarket dbResult:{}", result);
            return Result.success("success");
        } catch (Exception e) {
            log.error("QualityMarketService.deleteMarket error : {}", e.toString());
            return Result.fail(ErrorCode.unknownError);
        }
    }
    
    
    //查列表
    @Override
    public Result searchMarketList(String user, int pageSize, int pageNo, String creator, String marketName,
            String serviceName) {
        AppQualityMarket appQualityMarket = new AppQualityMarket();
        if (StringUtils.isNotEmpty(creator)) {
            appQualityMarket.setCreator(creator);
        }
        PageData pd = new PageData();
        pd.setPage(pageNo);
        pd.setPageSize(pageSize);
        pd.setTotal(appQualityMarketDao.getTotal(creator, marketName, serviceName));
        pd.setList(appQualityMarketDao.SearchAppQualityMarketList(pageNo, pageSize, creator, marketName, serviceName));
        return Result.success(pd);
    }
    
}
