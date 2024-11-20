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
package org.apache.ozhera.log.manager.common.validation;

import org.apache.ozhera.log.manager.dao.MilogLogTailDao;
import org.apache.ozhera.log.manager.dao.MilogLogstoreDao;
import org.apache.ozhera.log.manager.model.bo.LogTailParam;
import org.apache.ozhera.log.manager.model.bo.MlogParseParam;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import org.apache.ozhera.log.manager.service.extension.agent.MilogAgentService;
import org.apache.ozhera.log.manager.service.extension.agent.MilogAgentServiceFactory;
import org.apache.ozhera.log.manager.service.extension.resource.ResourceExtensionService;
import org.apache.ozhera.log.manager.service.extension.resource.ResourceExtensionServiceFactory;
import org.apache.ozhera.log.manager.service.extension.store.StoreExtensionService;
import org.apache.ozhera.log.manager.service.extension.store.StoreExtensionServiceFactory;
import org.apache.ozhera.log.manager.service.extension.tail.TailExtensionService;
import org.apache.ozhera.log.manager.service.extension.tail.TailExtensionServiceFactory;
import org.apache.ozhera.log.parse.LogParserFactory;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class HeraConfigValid {

    @Value("$server.type")
    private String serverType;

    @Resource
    private MilogLogstoreDao milogLogstoreDao;

    @Resource
    private MilogLogTailDao milogLogtailDao;

    private TailExtensionService tailExtensionService;

    private MilogAgentService milogAgentService;

    private StoreExtensionService storeExtensionService;

    private ResourceExtensionService resourceExtensionService;

    public void init() {
        tailExtensionService = TailExtensionServiceFactory.getTailExtensionService();
        milogAgentService = MilogAgentServiceFactory.getAgentExtensionService();
        storeExtensionService = StoreExtensionServiceFactory.getStoreExtensionService();
        resourceExtensionService = ResourceExtensionServiceFactory.getResourceExtensionService();
    }

    public String verifyLogTailParam(LogTailParam param) {
        if (null == param.getMilogAppId()) {
            return "The selected app cannot be empty";
        }
        if (null == param || StringUtils.isBlank(param.getLogPath())) {
            return "The path cannot be empty";
        }
        if (null == param.getSpaceId()) {
            return "The space ID cannot be empty";
        }
        if (null == param.getStoreId()) {
            return "The store ID cannot be empty";
        }
        String path = param.getLogPath();
        if (path.equals("/home/work/log/") || path.equals("/home/work/log") || path.startsWith("/home/work/log") && path.split("/").length < 4) {
            return "The log path is wrong, please confirm and submit";
        }
        String validMsg = tailExtensionService.validLogPath(param);
        return StringUtils.isNotEmpty(validMsg) ? validMsg : StringUtils.EMPTY;
    }

    public String checkParseExampleParam(MlogParseParam mlogParseParam) {
        StringBuilder sb = new StringBuilder();
        if (!LogParserFactory.LogParserEnum.JSON_PARSE.getCode().equals(mlogParseParam.getParseType())
                && (null == mlogParseParam.getParseScript() || "" == mlogParseParam.getParseScript())) {
            sb.append("The parsing script cannot be empty;");
        }
        if (null == mlogParseParam.getMsg()) {
            sb.append("Log information cannot be empty;");
        }
        return sb.toString();
    }

    public boolean checkTailNameSame(String tailName, Long id, String machineRoom, Long spaceId) {
        // Verify the log file with the same name
        List<MilogLogTailDo> logtailDoList = milogLogtailDao.queryTailNameExists(tailName, machineRoom, spaceId);
        if (null == id) {
            return CollectionUtils.isNotEmpty(logtailDoList);
        } else {
            if (CollectionUtils.isEmpty(logtailDoList)) {
                return false;
            }
            MilogLogTailDo milogLogtailDo = logtailDoList.get(logtailDoList.size() - 1);
            return !milogLogtailDo.getId().equals(id);
        }
    }
}
