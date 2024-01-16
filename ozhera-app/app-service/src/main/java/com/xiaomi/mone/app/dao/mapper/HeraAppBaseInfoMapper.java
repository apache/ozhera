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
package com.xiaomi.mone.app.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaomi.mone.app.api.model.HeraAppBaseInfoParticipant;
import com.xiaomi.mone.app.api.model.HeraAppBaseQuery;
import com.xiaomi.mone.app.api.response.AppBaseInfo;
import com.xiaomi.mone.app.model.HeraAppBaseInfo;
import com.xiaomi.mone.app.model.HeraAppBaseInfoExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/10/29 12:13
 */
@Component
public interface HeraAppBaseInfoMapper extends BaseMapper<HeraAppBaseInfo> {

    List<AppBaseInfo> queryAppInfo(@Param("appName") String appName, @Param("platformType") Integer platformType, @Param("type") Integer type);

    List<AppBaseInfo> queryByIds(List<Long> ids);

    List<HeraAppBaseInfoParticipant> selectByParticipant(HeraAppBaseQuery query);

    Long countByParticipant(HeraAppBaseQuery query);

    long countByExample(HeraAppBaseInfoExample example);

    int deleteByExample(HeraAppBaseInfoExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(HeraAppBaseInfo record);

    int insertSelective(HeraAppBaseInfo record);

    List<HeraAppBaseInfo> selectByExampleWithBLOBs(HeraAppBaseInfoExample example);

    List<HeraAppBaseInfo> selectByExample(HeraAppBaseInfoExample example);

    HeraAppBaseInfo selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") HeraAppBaseInfo record, @Param("example") HeraAppBaseInfoExample example);

    int updateByExampleWithBLOBs(@Param("record") HeraAppBaseInfo record, @Param("example") HeraAppBaseInfoExample example);

    int updateByExample(@Param("record") HeraAppBaseInfo record, @Param("example") HeraAppBaseInfoExample example);

    int updateByPrimaryKeySelective(HeraAppBaseInfo record);

    int updateByPrimaryKeyWithBLOBs(HeraAppBaseInfo record);

    int updateByPrimaryKey(HeraAppBaseInfo record);

    int batchInsert(@Param("list") List<HeraAppBaseInfo> list);

    int batchInsertSelective(@Param("list") List<HeraAppBaseInfo> list, @Param("selective") HeraAppBaseInfo.Column... selective);

    Long countNormalData();
}
