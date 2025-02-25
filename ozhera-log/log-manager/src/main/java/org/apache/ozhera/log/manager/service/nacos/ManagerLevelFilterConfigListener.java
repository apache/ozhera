package org.apache.ozhera.log.manager.service.nacos;

import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.nacos.api.exception.NacosException;
import com.xiaomi.data.push.common.SafeRun;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.plugin.nacos.NacosConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.apache.ozhera.log.common.Constant.*;

@Slf4j
@Component
public class ManagerLevelFilterConfigListener {

    @Resource
    private NacosConfig nacosConfig;

    @Resource
    private MilogLogTailDao logtailDao;

    private final String logLevelFilterKey = "log.level.filter.config.manager";

    private volatile ManagerLogFilterConfig managerLogFilterConfig;

    public void init() {
        ScheduledExecutorService scheduledExecutor = Executors
                .newSingleThreadScheduledExecutor(ThreadUtil.newNamedThreadFactory("log-level-filter-manager", false));
        scheduledExecutor.scheduleAtFixedRate(() ->
                SafeRun.run(() -> configChangeOperator()), 0, 1, TimeUnit.MINUTES);

    }

    public void configChangeOperator() throws NacosException {
        String filterConfig = nacosConfig.getConfigStr(logLevelFilterKey, DEFAULT_GROUP_ID, DEFAULT_TIME_OUT_MS);
        ManagerLogFilterConfig newManagerLogFilterConfig = GSON.fromJson(filterConfig, ManagerLogFilterConfig.class);
        //两者都为空，或者两者都不为空但是属性的值都相等
        if (Objects.equals(managerLogFilterConfig, newManagerLogFilterConfig))
            return;

        if (managerLogFilterConfig != null && managerLogFilterConfig.getEnableGlobalFilter()) {
            List<Long> oldTailIdList = managerLogFilterConfig.getTailIdList();
            List<MilogLogTailDo> oldMilogLogtailList = logtailDao.getMilogLogtail(oldTailIdList);
            oldMilogLogtailList.forEach(tail -> {
                tail.setCollectedLogLevelList(new ArrayList<>());
                logtailDao.update(tail);
            });
        }
        if (newManagerLogFilterConfig != null && newManagerLogFilterConfig.getEnableGlobalFilter()) {
            List<Long> newTailIdList = newManagerLogFilterConfig.getTailIdList();
            List<MilogLogTailDo> newMilogLogtailList = logtailDao.getMilogLogtail(newTailIdList);
            newMilogLogtailList.forEach(tail -> {
                tail.setCollectedLogLevelList(newManagerLogFilterConfig.getLogLevelList());
                logtailDao.update(tail);
            });
        }
        managerLogFilterConfig = newManagerLogFilterConfig;
    }

    public ManagerLogFilterConfig queryFilterConfig() {
        return managerLogFilterConfig;
    }

}
