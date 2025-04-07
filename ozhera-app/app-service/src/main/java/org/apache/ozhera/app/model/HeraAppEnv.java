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
package org.apache.ozhera.app.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import org.apache.ozhera.app.api.model.HeraSimpleEnv;
import org.apache.ozhera.app.enums.OperateEnum;
import org.apache.ozhera.app.exception.AppException;
import org.apache.ozhera.app.model.vo.HeraAppEnvVo;
import org.apache.ozhera.app.model.vo.HeraAppOperateVo;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

import java.time.Instant;
import java.util.List;
import java.util.Random;

/**
 * @description app和app部署后对应的环境相关的信息（这些信息可能来自于别的系统同步所致）
 */
@Data
@TableName(value = "hera_app_env", autoResultMap = true)
public class HeraAppEnv extends BaseCommon {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long heraAppId;

    private Long appId;
    /**
     * 冗余信息，防止查询时需要
     */
    private String appName;

    private Long envId;

    private String envName;

    @TableField(value = "ip_list", typeHandler = JacksonTypeHandler.class)
    private List<String> ipList;

    public HeraAppEnvVo toHeraAppEnvVo() {
        HeraAppEnvVo heraAppEnvVo = new HeraAppEnvVo();
        try {
            BeanUtils.copyProperties(this, heraAppEnvVo);
            return heraAppEnvVo;
        } catch (Exception e) {
            throw new AppException("数据转化异常", e);
        }
    }

    public HeraAppEnv operateVoToHeraAppEnv(HeraAppOperateVo operateVo, OperateEnum operateEnum) {
        if(StringUtils.isBlank(operateVo.getEnvName())){
            operateVo.setEnvName("staging");
        }
        if(null == operateVo.getEnvId()){
            operateVo.setEnvId((long) new Random().nextInt(400_0));
        }
        BeanUtils.copyProperties(operateVo, this);
        if (OperateEnum.ADD_OPERATE == operateEnum) {
            this.setCtime(Instant.now().toEpochMilli());
            this.setCreator(operateVo.getOperator());
        }
        this.setUtime(Instant.now().toEpochMilli());
        this.setUpdater(operateVo.getOperator());
        return this;
    }


    public HeraSimpleEnv toHeraSimpleEnv() {
        return HeraSimpleEnv.builder()
                .id(this.id)
                .name(this.envName)
                .ips(this.ipList)
                .build();
    }
}
