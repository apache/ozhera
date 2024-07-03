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
package run.mone.chaos.operator.dao.impl;

import com.xiaomi.youpin.docean.anno.Component;
import run.mone.chaos.operator.constant.ModeEnum;
import run.mone.chaos.operator.constant.TaskEnum;
import run.mone.chaos.operator.dao.domain.ChaosTask;
import run.mone.chaos.operator.dao.domain.ChaosTaskReport;
import run.mone.chaos.operator.dto.page.PageData;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-16 14:11
 */
@Component
public class ChaosTaskReportDao extends BaseDao {

    private static final String REPORT_NAME_TEMPLATE = "%s-%s-%s-%s";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");


    public PageData<ChaosTaskReport> getListByMap(PageData<ChaosTaskReport> pageData, Map<String, Object> kvMap) {
        return getListByMap(kvMap, pageData, ChaosTaskReport.class);
    }

    public String createReport(ChaosTaskReport report) {
        return insert(report).toString();
    }

}
