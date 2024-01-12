package com.xiaomi.mone.log.manager.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/9/19 15:45
 */
@Data
public class PageVo implements Serializable {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}
