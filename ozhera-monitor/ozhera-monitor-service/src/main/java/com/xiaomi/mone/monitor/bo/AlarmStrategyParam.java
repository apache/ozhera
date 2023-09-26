package com.xiaomi.mone.monitor.bo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 *
 * @author zhanggaofeng1
 */
@ToString
@Data
public class AlarmStrategyParam {

    private Integer id;
    private List<Integer> ids;
    private Integer appId;
    private String appName;
    private Integer strategyType;
    private String strategyName;
    private String desc;
    private Integer status;//0 usable，1 forbidden
    private int page;
    private int pageSize;
    private String sortBy;
    private String sortOrder;
    private boolean templateNeed;
    private boolean ruleNeed;
    private boolean owner;

    public void pageQryInit() {
        if (page <= 0) {
            page = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        if (pageSize >= 100) {
            pageSize = 100;
        }
    }

}
