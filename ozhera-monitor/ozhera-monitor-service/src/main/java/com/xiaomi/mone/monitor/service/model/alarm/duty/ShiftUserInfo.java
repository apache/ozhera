package com.xiaomi.mone.monitor.service.model.alarm.duty;

import lombok.Data;

import java.io.Serializable;

/**
 * @author gaoxihui
 * @date 2023/6/9 2:56 下午
 */
@Data
public class ShiftUserInfo implements Serializable {

    private String user;//Duty person email prefix
    private String display_name;//User alias (Chinese name)
    private Integer start_time;//Start time
    private Long end_time;//End of watch time
    private Long acquire_time;//Time of claim, 10-digit timestamp. 0 indicates that it is not claimed
    private Integer acquire_status;//Claim status, 0= unclaimed, 1= claimed
    private Boolean replace_duty;//The value is true, false, or empty
    private UserInfo should_oncall_user;//The original person on duty during that time

}
