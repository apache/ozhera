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

package org.apache.ozhera.monitor.service.impl;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.apache.ozhera.monitor.bo.RulePromQLTemplateInfo;
import org.apache.ozhera.monitor.bo.RulePromQLTemplateParam;
import org.apache.ozhera.monitor.dao.RulePromQLTemplateDao;
import org.apache.ozhera.monitor.dao.model.RulePromQLTemplate;
import org.apache.ozhera.monitor.result.ErrorCode;
import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.RulePromQLTemplateService;
import org.apache.ozhera.monitor.service.model.PageData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author zhanggaofeng1
 */

@Slf4j
@Service
public class RulePromQLTemplateServiceImpl implements RulePromQLTemplateService {
    
    @Autowired
    private RulePromQLTemplateDao rulePromQLTemplateDao;
    
    @NacosValue(value = "${prometheus.url}", autoRefreshed = true)
    private String prometheusUrl;
    
    @NacosValue(value = "${prometheus.check.url}", autoRefreshed = true)
    private String prometheusCheckUrl;
    
    /**
     * 添加模板
     *
     * @param user
     * @param param
     * @return
     */
    @Override
    public Result add(String user, RulePromQLTemplateParam param) {
        List<RulePromQLTemplate> templates = rulePromQLTemplateDao.getByName(user, param.getName());
        if (!CollectionUtils.isEmpty(templates)) {
            Result result = Result.fail(ErrorCode.invalidParamError);
            result.setMessage("名称不允许重复");
            return result;
        }
        RulePromQLTemplate template = new RulePromQLTemplate();
        BeanUtils.copyProperties(param, template);
        template.setCreater(user);
        if (!rulePromQLTemplateDao.insert(template)) {
            return Result.fail(ErrorCode.unknownError);
        }
        log.info("add PromQL template 成功：user={}, template={}", user, template);
        return Result.success(null);
    }
    
    /**
     * 编辑模板
     *
     * @param user
     * @param param
     * @return
     */
    @Override
    public Result edit(String user, RulePromQLTemplateParam param) {
        List<RulePromQLTemplate> templates = rulePromQLTemplateDao.getByName(user, param.getName());
        if (templates != null && templates.size() > 1) {
            Result result = Result.fail(ErrorCode.invalidParamError);
            result.setMessage("名称不允许重复");
            return result;
        }
        RulePromQLTemplate template = new RulePromQLTemplate();
        BeanUtils.copyProperties(param, template);
        template.setCreater(null);
        if (!rulePromQLTemplateDao.updateById(template)) {
            return Result.fail(ErrorCode.unknownError);
        }
        log.info("updateById PromQL template 成功：user={}, template={}", user, template);
        return Result.success(null);
    }
    
    @Override
    public Result deleteById(String user, Integer templateId) {
        if (!rulePromQLTemplateDao.deleteById(templateId)) {
            return Result.fail(ErrorCode.unknownError);
        }
        log.info("deleteById PromQL template 成功：user={}, templateId={}", user, templateId);
        return Result.success(null);
    }
    
    @Override
    public Result<PageData<List<RulePromQLTemplateInfo>>> search(String user, RulePromQLTemplateParam param) {
        RulePromQLTemplate template = new RulePromQLTemplate();
        PageData<List<RulePromQLTemplateInfo>> pageData = rulePromQLTemplateDao.searchByCond(user, param);
        log.info("query promQL template user={}, param={}, pageData={}", user, param, pageData);
        return Result.success(pageData);
    }
    
    @Override
    public Result<String> testPromQL(String user, RulePromQLTemplateParam param) throws UnsupportedEncodingException {
        StringBuilder url = new StringBuilder();
        url.append(prometheusCheckUrl).append("graph?g0.expr=").append(URLEncoder.encode(param.getPromql(), "UTF-8"))
                .append("&g0.tab=1&g0.stacked=0&g0.range_input=1h");
        return Result.success(url.toString());
    }
    
}
