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
import com.xiaomi.mone.app.api.model.HeraMetaDataModel;
import com.xiaomi.mone.app.api.model.HeraMetaDataQuery;
import com.xiaomi.mone.app.api.service.HeraMetaDataService;
import com.xiaomi.mone.app.dao.mapper.HeraMetaDataMapper;
import com.xiaomi.mone.app.model.HeraMetaData;
import com.xiaomi.mone.app.util.HeraMetaDataConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.util.List;

/**
 * @Description
 * @Author dingtao
 * @Date 2023/4/28 2:09 PM
 */
@Slf4j
@Service(registry = "registryConfig", interfaceClass = HeraMetaDataService.class, group = "${dubbo.group}")
@org.springframework.stereotype.Service
public class HeraMetaDataServiceImpl implements HeraMetaDataService {

    private HeraMetaDataMapper heraMetaDataMapper;

    public HeraMetaDataServiceImpl(HeraMetaDataMapper heraMetaDataMapper) {
        this.heraMetaDataMapper = heraMetaDataMapper;
    }

    @Override
    public int count(HeraMetaDataQuery query) {
        return heraMetaDataMapper.selectCount(new QueryWrapper());
    }

    @Override
    public List<HeraMetaDataModel> page(HeraMetaDataQuery query) {
        QueryWrapper<HeraMetaData> wrapper = new QueryWrapper();
        if(query.getId() != null){
            wrapper.gt("id",query.getId()).last("LIMIT "+query.getPageSize());
        }else {
            query.initPageParam();
            if (query.getLimit() != null && query.getLimit() > 0) {
                wrapper.last("LIMIT " + query.getOffset() + " , " + query.getLimit());
            }
        }
        List<HeraMetaData> heraMetaData = heraMetaDataMapper.selectList(wrapper);
        return HeraMetaDataConvertUtil.convertToModel(heraMetaData);
    }

    @Override
    public int insert(HeraMetaDataModel model) {
        if(model == null) {
            return 0;
        }
        HeraMetaData heraMetaData = HeraMetaDataConvertUtil.modelConvertTo(model);
        if(heraMetaData == null){
            return 0;
        }
        return heraMetaDataMapper.insert(heraMetaData);
    }

    @Override
    public int insert(List<HeraMetaDataModel> models) {
        if(models == null || models.size() == 0) {
            return 0;
        }
        int result = 0;
        for(HeraMetaDataModel model : models){
            result += insert(model);
        }
        return result;
    }

    @Override
    public int delete(int metaId) {
        QueryWrapper<HeraMetaData> wrapper = new QueryWrapper<>();
        wrapper.eq("meta_id", metaId);
        return heraMetaDataMapper.delete(wrapper);
    }

}
