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
package org.apache.ozhera.monitor.bo.bizmetrics;

import lombok.Data;
import org.apache.ozhera.monitor.service.model.PageData;

import java.util.List;
import java.util.Map;

/**
 * 增强的分页数据，包含字段元数据信息和查询示例
 */
@Data
public class EnhancedPageData<T> {
    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 字段元数据列表
     */
    private List<MetricDataField> fields;

    /**
     * 查询条件示例
     */
    private Map<String, Object> queryExamples;

    /**
     * 从PageData转换
     */
    public static <T> EnhancedPageData<T> from(PageData pageData) {
        EnhancedPageData<T> enhancedData = new EnhancedPageData<>();
        enhancedData.setTotal(pageData.getTotal());
        enhancedData.setPage(pageData.getPage());
        enhancedData.setPageSize(pageData.getPageSize());
        enhancedData.setList((List<T>) pageData.getList());
        return enhancedData;
    }
}