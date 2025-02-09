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
package org.apache.ozhera.monitor.dao;

import org.apache.ozhera.monitor.dao.mapper.AppQualityMarketMapper;
import org.apache.ozhera.monitor.dao.model.AppQualityMarket;
import org.apache.ozhera.monitor.dao.model.AppQualityMarketExample;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * @author zhangxiaowei6
 */

@Slf4j
@Repository
public class AppQualityMarketDao {
    @Autowired
    private AppQualityMarketMapper appQualityMarketMapper;

    //插入一条大盘数据
    public int insertServiceMarket(AppQualityMarket appQualityMarket) {
        appQualityMarket.setCreateTime(new Date());
        appQualityMarket.setUpdateTime(new Date());
        try {
            int result = appQualityMarketMapper.insert(appQualityMarket);
            if (result < 0) {
                log.warn("[AppQualityMarketDao.insert] failed to insert AppQualityMarketDao: {}", appQualityMarket.toString());
                return 0;
            }
        } catch (Exception e) {
            log.error("[AppQualityMarketDao.insert] failed to insert AppQualityMarketDao: {}, err: {}", appQualityMarket.toString(), e);
            return 0;
        }
        return 1;
    }

    //查看一条大盘数据
    public AppQualityMarket SearchAppQualityMarket(Integer id) {
        try {
            AppQualityMarket result = appQualityMarketMapper.selectByPrimaryKey(id);
            if (result == null) {
                log.warn("[AppQualityMarketDao.search] failed to search AppQualityMarketDao id: {}", id);
            }
            return result;
        } catch (Exception e) {
            log.error("[AppQualityMarketDao.search] failed to search err: {} ,id: {}", e, id);
            return null;
        }
    }

    //更新一条大盘数据
    public int updateQualityMarket(AppQualityMarket appQualityMarket) {
        appQualityMarket.setUpdateTime(new Date());
        try {
            int result = appQualityMarketMapper.updateByPrimaryKey(appQualityMarket);
            if (result < 0) {
                log.warn("[AppQualityMarketDao.update] failed to update AppQualityMarketDao: {}", appQualityMarket.toString());
                return 0;
            }
            return result;
        } catch (Exception e) {
            log.error("[AppQualityMarketDao.update] failed to update AppQualityMarketDao : {} err: {}", appQualityMarket.toString(), e);
            return 0;
        }
    }

    //删除一条大盘数据
    public int deleteQualityMarket(Integer id) {
        try {
            int result = appQualityMarketMapper.deleteByPrimaryKey(id);
            if (result < 0) {
                log.warn("[AppQualityMarketDao.update] failed to delete AppQualityMarketDao id: {}", id);
                return 0;
            }
            return result;
        } catch (Exception e) {
            log.error("[AppQualityMarketDao.update] failed to delete AppQualityMarketDao id: {} err: {}", id, e);
            return 0;
        }
    }

    //获取list
    public List<AppQualityMarket> SearchAppQualityMarketList(int pageNo, int pageSize, String creator, String marketName, String serviceName) {
        AppQualityMarketExample aje = new AppQualityMarketExample();
        aje.setOrderByClause("id desc");
        aje.setLimit(pageSize);
        aje.setOffset((pageNo-1) * pageSize);
        AppQualityMarketExample.Criteria ca  = aje.createCriteria();
        if (StringUtils.isNotEmpty(creator) ) {
            ca.andCreatorLike("%" + creator + "%");
        }
        if (StringUtils.isNotEmpty(marketName) ) {
            ca.andMarketNameLike("%" + marketName + "%");
        }
        if (StringUtils.isNotEmpty(serviceName) ) {
            ca.andServiceListLike("%" + serviceName + "%");
        }

        try {
            List<AppQualityMarket> list = appQualityMarketMapper.selectByExample(aje);
            if (list == null) {
                log.warn("[AppQualityMarketDao.SearchAppQualityMarketList failed to search");
            }
            return list;
        }catch (Exception e) {
            log.error("[AppQualityMarketDao.SearchAppQualityMarketList failed to search err: {}",e.toString());
            return null;
        }
    }

    //获取总数
    public Long getTotal(String creator,String marketName,String serviceName) {
        AppQualityMarketExample aje = new AppQualityMarketExample();
        AppQualityMarketExample.Criteria ca  = aje.createCriteria();
        if (StringUtils.isNotEmpty(creator) ) {
            ca.andCreatorLike("%" + creator + "%");
        }
        if (StringUtils.isNotEmpty(marketName) ) {
            ca.andMarketNameLike("%" + marketName + "%");
        }
        if (StringUtils.isNotEmpty(serviceName) ) {
            ca.andServiceListLike("%" + serviceName + "%");
        }
        try {
            Long result = appQualityMarketMapper.countByExample(aje);
            if (result == null) {
                log.warn("[AppQualityMarketDao.getTotal failed");
            }
            return result;
        }catch (Exception e) {
            log.error("[AppQualityMarketDao.getTotal failed err: {}",e.toString());
        }
        return null;
    }

}
