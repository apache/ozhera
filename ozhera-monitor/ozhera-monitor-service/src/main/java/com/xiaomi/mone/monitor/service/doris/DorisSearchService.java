package com.xiaomi.mone.monitor.service.doris;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.mone.monitor.service.model.prometheus.MetricDetail;
import com.xiaomi.mone.tpc.common.util.GsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import run.mone.doris.DorisService;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class DorisSearchService {

    @NacosValue(value = "${doris.driver}",autoRefreshed = true)
    private String dorisDriver;

    @NacosValue(value = "${doris.url}",autoRefreshed = true)
    private String dorisUrl;

    @NacosValue(value = "${doris.username}",autoRefreshed = true)
    private String username;

    @NacosValue(value = "${doris.password}",autoRefreshed = true)
    private String password;

    private DorisService dorisService;
    @PostConstruct
    private void init(){
        dorisService = new DorisService(dorisDriver, dorisUrl, username, password);
    }

    public List<Map<String, Object>> queryBySql(String sql) throws SQLException {
        List<Map<String, Object>> query = dorisService.query(sql);
        return query;
    }

}
