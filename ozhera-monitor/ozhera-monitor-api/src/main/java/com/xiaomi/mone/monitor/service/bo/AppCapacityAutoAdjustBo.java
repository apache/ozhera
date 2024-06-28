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
package com.xiaomi.mone.monitor.service.bo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

@Data
@ToString
public class AppCapacityAutoAdjustBo implements Serializable {

    private Integer appId;//Application ID

    private Integer pipelineId;//Pipeline ID

    private String container;//Container Name

    private Integer minInstance;//Minimum number of instances

    private Integer maxInstance;//Maximum number of instances

    private Integer autoCapacity;//Auto-scaling: 1 for Yes, 0 for No

    private Integer dependOn;//Scaling criteria: 0 for CPU, 1 for Memory, 2 for CPU and Memory

}