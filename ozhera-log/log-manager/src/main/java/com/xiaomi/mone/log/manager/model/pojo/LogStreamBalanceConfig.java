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
