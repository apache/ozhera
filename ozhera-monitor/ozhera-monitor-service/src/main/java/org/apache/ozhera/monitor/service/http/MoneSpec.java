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

package org.apache.ozhera.monitor.service.http;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author gaoxihui
 * @date 2022/5/30 6:42 PM
 */
@Data
@ToString
public class MoneSpec implements Serializable {

    private String container;
    private String namespace;//命名空间
    private Integer replicas;//当前副本数
    private Integer setReplicas;//扩缩容后的副本书
    private Integer envID;//环境id
    private Long time;

    public void init(){
        time = System.currentTimeMillis();
    }

}
