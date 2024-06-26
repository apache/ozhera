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
package run.mone.chaos.operator.controller;

import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import com.xiaomi.youpin.docean.anno.RequestParam;
import com.xiaomi.youpin.docean.mvc.MvcResult;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.bo.ChaosMetricsBo;
import run.mone.chaos.operator.constant.StatusEnum;
import run.mone.chaos.operator.service.ChaosMetricsService;
import run.mone.chaos.operator.vo.PodLogUrlVO;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangxiaowei6
 * @Date 2024/4/17 10:27
 */
@Controller
@Slf4j
public class ChaosMetricsController {

    @Resource
    private ChaosMetricsService chaosMetricsService;

    @RequestMapping(path = "/chaosMetrics/getGoingGrafanaUrl", method = "get")
    public MvcResult<Map<String,String>> getGoingGrafanaUrl(@RequestParam(value = "id") String id, @RequestParam(value = "executedTimes")
    int executedTimes, @RequestParam(value = "status") int status) {
        MvcResult mvcResult = new MvcResult();
        String checkRes = preCheck(status, executedTimes);
        if (!"ok".equals(checkRes)) {
            log.info("getGoingGrafanaUrl preCheck error:{}",checkRes);
            mvcResult.setCode(500);
            mvcResult.setMessage("出错啦");
            return mvcResult;
        }

        mvcResult.setData(chaosMetricsService.getGoingGrafanaUrl(id, executedTimes, status));
        return mvcResult;
    }


    @RequestMapping(path = "/chaosMetrics/getTaskLogUrl", method = "get")
    public MvcResult<List<PodLogUrlVO>> getTaskLogUrl(@RequestParam(value = "id") String id) {
        MvcResult mvcResult = new MvcResult();
        mvcResult.setData(chaosMetricsService.getTaskLogUrl(id));
        return mvcResult;
    }

    private String preCheck(int status, int count) {
        if (count <= 0) {
            return "The experiment was performed at least once";
        }
        if (status != StatusEnum.actioning.type() && status != StatusEnum.recovered.type()) {
            return "The experiment status is not actioning or recovered";
        }
        return "ok";
    }
}
