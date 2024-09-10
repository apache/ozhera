/*
 *  Copyright (C) 2020 Xiaomi Corporation
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.xiaomi.mone.monitor.service;

import com.xiaomi.mone.monitor.bo.RulePromQLTemplateInfo;
import com.xiaomi.mone.monitor.bo.RulePromQLTemplateParam;
import com.xiaomi.mone.monitor.result.Result;
import com.xiaomi.mone.monitor.service.model.PageData;

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
