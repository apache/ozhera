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
package run.mone.chaos.operator.dao.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-01-03 15:08
 */
@Data
public class TimePO implements Serializable {

    // 这个值用mongo里边的创建任务的id代替
    private String uid;

    private String instanceId;

    private Long timeOffset;

    private long clkIdsMask;

    private List<IpAndUid> ipAndUids;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IpAndUid {
        String ip;
        String uid;
    }

}
