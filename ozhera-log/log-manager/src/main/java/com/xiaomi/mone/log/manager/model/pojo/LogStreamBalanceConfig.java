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
package com.xiaomi.mone.log.manager.model.pojo;

import com.xiaomi.mone.log.manager.model.BaseCommon;
import lombok.Data;
import org.nutz.dao.entity.annotation.*;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/16 15:22
 */
@Table("milog_stream_balance")
@Comment("日志消费均衡器")
@Data
public class LogStreamBalanceConfig extends BaseCommon {

    @Id
    @Comment("主键Id")
    @ColDefine(customType = "bigint")
    private Long id;

    @Column(value = "machine_room")
    @ColDefine(type = ColType.VARCHAR)
    @Comment("区域")
    private String machineRoom;

    @Column(value = "stream_server_name")
    @ColDefine(type = ColType.VARCHAR)
    @Comment("stream启动的服务名")
    private String streamServerName;

    @Column(value = "stream_balance_data_id")
    @ColDefine(type = ColType.VARCHAR)
    @Comment("nacos中的分配stream的dataId")
    private String streamBalanceDataId;

    @Column(value = "old_stream_key")
    @ColDefine(type = ColType.VARCHAR)
    @Comment("old-stream的key")
    private String oldStreamKey;

    @Column(value = "status")
    @ColDefine(type = ColType.INT, width = 10)
    @Comment("0.开启 1.关闭")
    private Integer status;
}
