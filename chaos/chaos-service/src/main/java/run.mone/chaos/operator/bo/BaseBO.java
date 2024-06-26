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
package run.mone.chaos.operator.bo;

import run.mone.chaos.operator.dto.page.PageData;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/15 11:17
 */
public abstract class BaseBO implements Serializable {
    private Integer page;
    private Integer pageSize;

    public <T> PageData<T> buildPageData() {
        return buildPageData(100);
    }

    public <T> PageData<T> buildPageData(int maxPageSize) {
        PageData<T> pageData = new PageData<>();
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 100;
        }
        if (page <= 0) {
            pageData.setPage(1);
        } else {
            pageData.setPage(page);
        }
        //设置最大页
        if (pageSize <= 0) {
            pageData.setPageSize(100);
        } else if (pageSize <= maxPageSize) {
            pageData.setPageSize(pageSize);
        } else {
            pageData.setPageSize(maxPageSize);
        }
        return pageData;
    }

    public Optional<String> paramValidate() {
        return Optional.empty();
    }

}
