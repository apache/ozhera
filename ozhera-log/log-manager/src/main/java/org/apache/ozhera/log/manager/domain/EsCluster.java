/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ozhera.log.manager.domain;

import org.apache.ozhera.log.common.Constant;
import org.apache.ozhera.log.manager.bootstrap.LogStoragePlugin;
import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.mapper.MilogEsClusterMapper;
import org.apache.ozhera.log.manager.model.pojo.MilogEsClusterDO;
import org.apache.ozhera.log.manager.service.extension.store.StoreExtensionService;
import org.apache.ozhera.log.manager.service.extension.store.StoreExtensionServiceFactory;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.common.StringUtils;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class EsCluster {
    @Resource
    private MilogEsClusterMapper esClusterMapper;

    @Resource
    private LogStoragePlugin logStoragePlugin;

    private StoreExtensionService storeExtensionService;

    public void init() {
        storeExtensionService = StoreExtensionServiceFactory.getStoreExtensionService();
    }

    /**
     * Get the ES client
     *
     * @param esClusterId
     * @return
     */
    public EsService getEsService(Long esClusterId) {
        if (esClusterId == null) {
            return null;
        }

        String beanName = Constant.LOG_STORAGE_SERV_BEAN_PRE + esClusterId;

        if (isBeanInitialized(esClusterId, beanName)) {
            return Ioc.ins().getBean(beanName);
        }

        return null;
    }

    private boolean isBeanInitialized(Long esClusterId, String beanName) {
        if (Ioc.ins().containsBean(beanName)) {
            return true;
        }

        logStoragePlugin.initializeLogStorage(getById(esClusterId));
        return Ioc.ins().containsBean(beanName);
    }

    /**
     * Get the ES client
     *
     * @return
     */
    public EsService getEsService() {
        MilogEsClusterDO curEsCluster = this.getCurEsCluster();
        if (curEsCluster == null) {
            return null;
        }
        if (Ioc.ins().containsBean(Constant.LOG_STORAGE_SERV_BEAN_PRE + curEsCluster.getId())) {
            return Ioc.ins().getBean(Constant.LOG_STORAGE_SERV_BEAN_PRE + curEsCluster.getId());
        } else {
            return null;
        }
    }

    /**
     * Obtain the ES client corresponding to the current user
     *
     * @return
     */
    public MilogEsClusterDO getCurEsCluster() {
        List<MilogEsClusterDO> esClusterList = esClusterMapper.selectByTag(MoneUserContext.getCurrentUser().getZone());
        MilogEsClusterDO cluster = esClusterList == null || esClusterList.isEmpty() ? null : esClusterList.get(0);
        log.info("[EsCluster.getCurEsCluster] user is {}, cluster is {}", MoneUserContext.getCurrentUser(), cluster.getName());
        return cluster;
    }

    public MilogEsClusterDO getById(Long id) {
        return esClusterMapper.selectById(id);
    }

    public MilogEsClusterDO getByRegion(String region) {
        if (StringUtils.isEmpty(region)) {
            return null;
        }
        MilogEsClusterDO esClusterDO = esClusterMapper.selectByRegion(region);
        return esClusterDO;
    }

    // Obtain the supported ES clusters in your region
    public MilogEsClusterDO getByArea4China(String area) {
        if (StringUtils.isEmpty(area)) {
            return null;
        }
        List<MilogEsClusterDO> clusterList = esClusterMapper.selectByArea(area, storeExtensionService.getMangerEsLabel());
        if (clusterList == null || clusterList.isEmpty()) {
            return null;
        }
        if (clusterList.size() > 1) {
            String zone = MoneUserContext.getCurrentUser().getZone();
            for (MilogEsClusterDO clusterDO : clusterList) {
                if (Objects.equals(zone, clusterDO.getTag())) {
                    return clusterDO;
                }
            }
        }
        return clusterList.get(0);
    }
}
