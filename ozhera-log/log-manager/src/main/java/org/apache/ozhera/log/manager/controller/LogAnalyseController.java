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
package org.apache.ozhera.log.manager.controller;

import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.model.dto.DashboardDTO;
import org.apache.ozhera.log.manager.model.dto.GraphDTO;
import org.apache.ozhera.log.manager.model.dto.GraphTypeDTO;
import org.apache.ozhera.log.manager.model.dto.LogAnalyseDataDTO;
import org.apache.ozhera.log.manager.model.vo.*;
import org.apache.ozhera.log.manager.service.impl.LogAnalyseService;
import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import com.xiaomi.youpin.docean.anno.RequestParam;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class LogAnalyseController {
    @Resource
    private LogAnalyseService logAnalyseService;

    @RequestMapping(path = "/log/analyse/show")
    public Result<DashboardDTO> logQuery(LogAnalyseQuery logAnalyseQuery) throws Exception {
        return logAnalyseService.getDashboardGraph(logAnalyseQuery);
    }

    @RequestMapping(path = "/log/analyse/data")
    public Result<LogAnalyseDataDTO> data(LogAnalyseDataQuery query) throws Exception {
        return logAnalyseService.data(query);
    }

    @RequestMapping(path = "/log/analyse/dataPre")
    public Result<LogAnalyseDataDTO> dataPre(LogAnalyseDataPreQuery query) throws Exception {
        return logAnalyseService.dataPre(query);
    }

    @RequestMapping(path = "/log/analyse/type")
    public Result<List<GraphTypeDTO>> type() throws Exception {
        return logAnalyseService.type();
    }

    @RequestMapping(path = "/log/analyse/key", method = "get")
    public Result<List<String>> supportKey(@RequestParam("storeId") Long storeId) throws Exception {
        return logAnalyseService.supportKey(storeId);
    }

    @RequestMapping(path = "/log/analyse/ref")
    public Result<Boolean> ref(DGRefCmd cmd) throws Exception {
        return logAnalyseService.ref(cmd);
    }

    @RequestMapping(path = "/log/analyse/delRef")
    public Result<Boolean> delRef(DGRefDelCmd cmd) throws Exception {
        return logAnalyseService.delRef(cmd);
    }

    @RequestMapping(path = "/log/analyse/updateRef")
    public Result<Boolean> updateRef(DGRefUpdateCmd cmd) throws Exception {
        return logAnalyseService.updateRef(cmd);
    }

    @RequestMapping(path = "/log/graph/create")
    public Result<Long> createGraph(CreateGraphCmd cmd) throws Exception {
        return logAnalyseService.createGraph(cmd);
    }

    @RequestMapping(path = "/log/graph/update")
    public Result<Boolean> createGraph(UpdateGraphCmd cmd) throws Exception {
        return logAnalyseService.updateGraph(cmd);
    }

    @RequestMapping(path = "/log/graph/delete")
    public Result<Boolean> deleteGraph(Long graphId) throws Exception {
        return logAnalyseService.deleteGraph(graphId);
    }

    @RequestMapping(path = "/log/graph/search")
    public Result<List<GraphDTO>> searchGraph(GraphQuery query) throws Exception {
        return logAnalyseService.searchGraph(query);
    }

    @RequestMapping(path = "/log/dashboard/create")
    public Result<Long> createDashboard(CreateDashboardCmd cmd) throws Exception {
        return logAnalyseService.createDashboard(cmd);
    }

}
