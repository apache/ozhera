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
package run.mone.chaos.operator.dto.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import run.mone.chaos.operator.vo.ChaosTaskInfoVO;

import java.io.Serializable;
import java.util.List;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/15 11:15
 */
@ToString
@Data
@NoArgsConstructor
public class PageData<T> implements Serializable {
    private int page;
    private int pageSize;
    private long total;
    private List<T> list;

    public PageData(List<T> chaosTaskInfoVOS, int pageSize, long total, int page) {
        this.pageSize = pageSize;
        this.total = total;
        this.page = page;
        this.list = chaosTaskInfoVOS;
    }
}
