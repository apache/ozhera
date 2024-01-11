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
package com.xiaomi.mone.log.manager.bootstrap;

import com.xiaomi.mone.log.api.enums.LogStorageTypeEnum;
import com.xiaomi.mone.log.common.Constant;
import com.xiaomi.mone.log.manager.mapper.MilogEsClusterMapper;
import com.xiaomi.mone.log.manager.model.pojo.MilogEsClusterDO;
import com.xiaomi.mone.log.manager.service.extension.store.DorisLogStorageService;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.DOceanPlugin;
import com.xiaomi.youpin.docean.plugin.IPlugin;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.datasource.pooled.PooledDataSource;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.List;

import static com.xiaomi.mone.log.common.Constant.GSON;

@DOceanPlugin
@Slf4j
public class LogStoragePlugin implements IPlugin {

    @Resource
    private MilogEsClusterMapper milogEsClusterMapper;

    @Value("$driver.class")
    private String driverClass;

    private static final String ADDR_PREFIX = "http://";

    @Override
    public void init() {
        log.info("es init start");
        List<MilogEsClusterDO> esClusterList = milogEsClusterMapper.selectAll();
        if (esClusterList == null || esClusterList.isEmpty()) {
            log.warn("no Log storage type");
            return;
        }
        for (MilogEsClusterDO cluster : esClusterList) {
            initializeLogStorage(cluster);
        }
    }

    public void initializeLogStorage(MilogEsClusterDO cluster) {
        LogStorageTypeEnum storageTypeEnum = LogStorageTypeEnum.queryByName(cluster.getLogStorageType());
        try {
            if (null == storageTypeEnum || LogStorageTypeEnum.ELASTICSEARCH == storageTypeEnum) {
                checkAddrUpdate(cluster);
                EsService esService = createEsService(cluster);
                registerEsService(cluster, esService);
                log.info("ES client[{}]Generated successfully[{}]", cluster.getName(), Constant.LOG_STORAGE_SERV_BEAN_PRE + cluster.getId());
            } else if (LogStorageTypeEnum.DORIS == storageTypeEnum) {
                DataSource dataSource = createDorisDataSource(cluster);
                registerDorisDataSource(cluster, dataSource);
                log.info("doris dataSource[{}]Generated successfully[{}]", cluster.getName(), Constant.LOG_STORAGE_SERV_BEAN_PRE + cluster.getId());
            }
        } catch (Exception e) {
            log.error("init storage client error,cluster{}", GSON.toJson(cluster), e);
        }
    }

    private EsService createEsService(MilogEsClusterDO cluster) {
        switch (cluster.getConWay()) {
            case Constant.ES_CONWAY_PWD:
                return new EsService(cluster.getAddr(), cluster.getUser(), cluster.getPwd());
            case Constant.ES_CONWAY_TOKEN:
                return new EsService(cluster.getAddr(), cluster.getToken(), cluster.getDtCatalog(), cluster.getDtDatabase());
            default:
                log.warn("The ES cluster entered an exception: [{}]", cluster);
                throw new IllegalArgumentException("Invalid ES connection way");
        }
    }

    private void registerEsService(MilogEsClusterDO cluster, EsService esService) {
        Ioc.ins().putBean(Constant.LOG_STORAGE_SERV_BEAN_PRE + cluster.getId(), esService);
    }

    private DataSource createDorisDataSource(MilogEsClusterDO cluster) {
        String addr = cluster.getAddr();
        PooledDataSource pooledDataSource = new PooledDataSource(driverClass, addr, cluster.getUser(), cluster.getPwd());
        pooledDataSource.setPoolPingEnabled(true);
        pooledDataSource.setPoolPingQuery("SELECT 1");
        pooledDataSource.setPoolMaximumActiveConnections(20);
        return pooledDataSource;
    }

    private void registerDorisDataSource(MilogEsClusterDO cluster, DataSource dataSource) {
        Ioc.ins().putBean(Constant.LOG_STORAGE_SERV_BEAN_PRE + cluster.getId(), dataSource);
    }

    private void checkAddrUpdate(MilogEsClusterDO cluster) {
        String addr = cluster.getAddr();
        if (addr.startsWith(ADDR_PREFIX)) {
            cluster.setAddr(addr.substring(ADDR_PREFIX.length() + 1));
        }
    }
}
