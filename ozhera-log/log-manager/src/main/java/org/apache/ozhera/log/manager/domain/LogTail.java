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

import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.mapper.MilogLogTemplateMapper;
import org.apache.ozhera.log.manager.model.pojo.MilogLogStoreDO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.apache.ozhera.log.manager.service.bind.LogTypeProcessor;
import org.apache.ozhera.log.manager.service.bind.LogTypeProcessorFactory;
import org.apache.ozhera.log.manager.service.extension.tail.TailExtensionService;
import org.apache.ozhera.log.manager.service.extension.tail.TailExtensionServiceFactory;
import org.apache.ozhera.log.manager.service.impl.LogTailServiceImpl;
import com.xiaomi.youpin.docean.anno.Service;
import org.apache.commons.collections.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LogTail {
    @Resource
    private MilogLogTailDao milogLogtailDao;

    @Resource
    private LogTailServiceImpl logTailService;

    @Resource
    private MilogLogstoreDao logStoreDao;

    @Resource
    private MilogLogTemplateMapper milogLogTemplateMapper;

    @Resource
    private LogTypeProcessorFactory logTypeProcessorFactory;

    private LogTypeProcessor logTypeProcessor;

    private TailExtensionService tailExtensionService;

    public void init() {
        logTypeProcessorFactory.setMilogLogTemplateMapper(milogLogTemplateMapper);
        logTypeProcessor = logTypeProcessorFactory.getLogTypeProcessor();
        tailExtensionService = TailExtensionServiceFactory.getTailExtensionService();
    }

    public void handleStoreTail(Long storeId) {
        MilogLogStoreDO milogLogStoreDO = logStoreDao.queryById(storeId);

        boolean supportedConsume = logTypeProcessor.supportedConsume(milogLogStoreDO.getLogType());

        List<MilogLogTailDo> milogLogtailDos = milogLogtailDao.queryTailsByStoreId(storeId);
        if (CollectionUtils.isNotEmpty(milogLogtailDos)) {
            milogLogtailDos.forEach(milogLogtailDo -> tailExtensionService.updateSendMsg(milogLogtailDo, milogLogtailDo.getIps(), supportedConsume));
        }
    }
}
