package com.xiaomi.mone.log.manager.dao;

import com.xiaomi.mone.log.manager.model.pojo.LogStreamBalanceConfig;
import com.xiaomi.youpin.docean.anno.Service;
import org.nutz.dao.Cnd;
import org.nutz.dao.impl.NutDao;

import javax.annotation.Resource;
import java.util.List;

import static com.xiaomi.mone.log.common.Constant.EQUAL_OPERATE;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/16 15:48
 */
@Service
public class LogStreamBalanceConfigDao {

    @Resource
    private NutDao dao;

    public List<LogStreamBalanceConfig> queryConfigEnabled() {
        return dao.query(LogStreamBalanceConfig.class, Cnd.where("status", EQUAL_OPERATE, 0));
    }

}
