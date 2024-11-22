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

package org.apache.ozhera.trace.etl.metadata.dubbo;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.ozhera.trace.etl.api.service.HeraMetaDataService;
import org.apache.ozhera.trace.etl.domain.metadata.HeraMetaData;
import org.apache.ozhera.trace.etl.domain.metadata.HeraMetaDataModel;
import org.apache.ozhera.trace.etl.domain.metadata.HeraMetaDataQuery;
import org.apache.ozhera.trace.etl.mapper.HeraMetaDataMapper;
import org.apache.ozhera.trace.etl.util.convert.HeraMetaDataConvert;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Service;

import java.util.List;


/**
 * @Author dingtao
 */
@Slf4j
@Service(interfaceClass = HeraMetaDataService.class, group = "${dubbo.group}")
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
            wrapper.gt("id",query.getId()).orderByAsc("id").last("LIMIT "+query.getPageSize());
        }else {
            query.initPageParam();
            if (query.getLimit() != null && query.getLimit() > 0) {
                wrapper.last("LIMIT " + query.getOffset() + " , " + query.getLimit());
            }
        }
        List<HeraMetaData> heraMetaData = heraMetaDataMapper.selectList(wrapper);
        return HeraMetaDataConvert.INSTANCE.toModelList(heraMetaData);
    }

    @Override
    public int insert(HeraMetaDataModel model) {
        if(model == null) {
            return 0;
        }
        HeraMetaData heraMetaData = HeraMetaDataConvert.INSTANCE.toBo(model);
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