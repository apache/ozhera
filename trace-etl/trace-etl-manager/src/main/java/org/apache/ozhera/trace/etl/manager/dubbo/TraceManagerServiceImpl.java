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

package org.apache.ozhera.trace.etl.manager.dubbo;

import org.apache.ozhera.trace.etl.api.service.TraceManagerService;
import org.apache.ozhera.trace.etl.domain.HeraTraceConfigVo;
import org.apache.ozhera.trace.etl.domain.HeraTraceEtlConfig;
import org.apache.ozhera.trace.etl.service.ManagerService;
import com.xiaomi.youpin.infra.rpc.Result;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/4/24 7:35 下午
 */
@Service(interfaceClass = TraceManagerService.class, group = "${dubbo.group}", version="1.0")
public class TraceManagerServiceImpl implements TraceManagerService {

    @Autowired
    private ManagerService managerService;

    @Override
    public Result<List<HeraTraceEtlConfig>> getAll() {
        HeraTraceConfigVo vo = new HeraTraceConfigVo();
        List<HeraTraceEtlConfig> all = managerService.getAll(vo);
        return Result.success(all);
    }
}