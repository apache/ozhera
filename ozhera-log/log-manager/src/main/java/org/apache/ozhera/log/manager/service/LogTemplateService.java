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
package org.apache.ozhera.log.manager.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.ozhera.log.common.Result;
import org.apache.ozhera.log.manager.model.dto.LogTemplateDTO;
import org.apache.ozhera.log.manager.model.dto.LogTemplateDetailDTO;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTemplateDO;

import java.io.IOException;
import java.util.List;

public interface LogTemplateService extends IService<MilogLogTemplateDO> {
    /**
     * Log template list
     *
     * @return
     */
    Result<List<LogTemplateDTO>> getLogTemplateList(String area);

    /**
     * Get log template
     *
     * @param logTemplateId
     * @return
     * @throws IOException
     */
    Result<LogTemplateDetailDTO> getLogTemplateById(long logTemplateId);
}
