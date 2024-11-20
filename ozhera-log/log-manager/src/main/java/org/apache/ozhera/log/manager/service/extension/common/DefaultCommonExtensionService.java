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
package org.apache.ozhera.log.manager.service.extension.common;

import org.apache.ozhera.log.api.enums.MQSourceEnum;
import org.apache.ozhera.log.api.enums.MachineRegionEnum;
import org.apache.ozhera.log.manager.model.vo.LogQuery;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.*;
import static org.apache.ozhera.log.manager.service.extension.common.CommonExtensionService.DEFAULT_COMMON_EXTENSION_SERVICE_KEY;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/4/27 16:13
 */
@Service(name = DEFAULT_COMMON_EXTENSION_SERVICE_KEY)
@Slf4j
public class DefaultCommonExtensionService implements CommonExtensionService {

    @Override
    public String getLogManagePrefix() {
        return LOG_MANAGE_PREFIX;
    }

    @Override
    public String getHeraLogStreamServerName() {
        return DEFAULT_STREAM_SERVER_NAME;
    }

    @Override
    public String getMachineRoomName(String machineRoomEn) {
        return MachineRegionEnum.queryCnByEn(machineRoomEn);
    }

    @Override
    public boolean middlewareEnumValid(Integer type) {
        return Arrays.stream(MQSourceEnum.values()).map(MQSourceEnum::getCode).toList().contains(type);
    }

    @Override
    public BoolQueryBuilder commonRangeQuery(LogQuery logQuery) {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("timestamp").from(logQuery.getStartTime()).to(logQuery.getEndTime()));
        boolQueryBuilder.filter(QueryBuilders.termQuery("storeId", logQuery.getStoreId()));
        return boolQueryBuilder;
    }

    @Override
    public String getSortedKey(LogQuery logQuery, String sortedKey) {
        return sortedKey;
    }

    @Override
    public TermQueryBuilder multipleChooseBuilder(DefaultCommonExtensionService.QueryTypeEnum queryTypeEnum, Long storeId, String chooseVal) {
        if (QueryTypeEnum.ID == queryTypeEnum) {
            return QueryBuilders.termQuery("tailId", chooseVal);
        }
        return QueryBuilders.termQuery("tail", chooseVal);
    }

    @Override
    public String queryDateHistogramField(Long storeId) {
        return "timestamp";
    }

    @Override
    public String getSearchIndex(Long logStoreId, String esIndexName) {
        return esIndexName;
    }

    @Override
    public String getSpaceDataId(Long spaceId) {
        return getLogManagePrefix() + NAMESPACE_CONFIG_DATA_ID;
    }

    @Override
    public List<String> queryMachineRegions() {
        return Arrays.stream(MachineRegionEnum.values()).map(MachineRegionEnum::getEn).collect(Collectors.toList());
    }

    @Getter
    public static enum QueryTypeEnum {
        ID,
        TEXT
    }
}
