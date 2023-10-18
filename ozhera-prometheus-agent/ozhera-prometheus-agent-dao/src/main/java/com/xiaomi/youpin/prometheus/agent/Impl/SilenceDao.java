package com.xiaomi.youpin.prometheus.agent.Impl;

import com.xiaomi.youpin.prometheus.agent.entity.RuleSilenceEntity;
import com.xiaomi.youpin.prometheus.agent.entity.ScrapeConfigEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * @author zhangxiaowei6
 * @Date 2023/10/17 14:28
 */
@Slf4j
@Repository
public class SilenceDao  extends BaseDao{

    public Long CreateSilence(RuleSilenceEntity entity) {
        return dao.insert(entity).getId();
    }
}
