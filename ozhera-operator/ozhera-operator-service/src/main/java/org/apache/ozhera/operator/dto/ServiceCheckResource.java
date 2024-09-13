/*
 * Copyright (C) 2020 Xiaomi Corporation
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
package org.apache.ozhera.operator.dto;

import lombok.Data;
import org.apache.ozhera.operator.bo.HeraResource;

import java.io.Serializable;

/**
 * @author shanwb
 * @date 2023-02-24
 */
@Data
public class ServiceCheckResource implements Serializable {

    private String serviceType;
    /**
     * 0: Creation completed.
     * 1：Creating
     */
    private Integer status;

    HeraResource heraResource;

}