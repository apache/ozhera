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

package org.apache.ozhera.monitor.service;

import org.apache.ozhera.monitor.bo.RulePromQLTemplateInfo;
import org.apache.ozhera.monitor.bo.RulePromQLTemplateParam;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.model.PageData;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author zhanggaofeng1
 */

public interface RulePromQLTemplateService {
    
    
    /**
     * 添加模板
     *
     * @param user
     * @param param
     * @return
     */
    Result add(String user, RulePromQLTemplateParam param);
    
    /**
     * 编辑模板
     *
     * @param user
     * @param param
     * @return
     */
    Result edit(String user, RulePromQLTemplateParam param);
    
    
    Result deleteById(String user, Integer templateId);
    
    Result<PageData<List<RulePromQLTemplateInfo>>> search(String user, RulePromQLTemplateParam param);
    
    Result<String> testPromQL(String user, RulePromQLTemplateParam param) throws UnsupportedEncodingException;
    
}
