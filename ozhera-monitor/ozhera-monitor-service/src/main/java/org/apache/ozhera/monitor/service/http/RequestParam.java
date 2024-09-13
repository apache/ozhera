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
package org.apache.ozhera.monitor.service.http;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author gaoxihui
 * @date 2022/5/30 6:35 PM
 */
@Data
public class RequestParam implements Serializable {

    private MoneSpec moneSpec;
    private String name;

    public void init(Integer recordId){
        name = recordId == null || recordId.intValue()==0 ? UUID.randomUUID().toString().replaceAll("-","") : String.valueOf(recordId);
    }
}
