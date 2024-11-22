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

package org.apache.ozhera.monitor.service.kubernetes;

import org.apache.ozhera.monitor.service.http.MoneSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author gaoxihui
 * @date 2022/6/7 9:51 AM
 */
@Slf4j
@Service
public class CapacityAdjustMessageService {

    private LinkedBlockingDeque<MoneSpec> queue = new LinkedBlockingDeque<>();

    public void product(MoneSpec moneSpec){
        try {
            queue.putLast(moneSpec);
        } catch (InterruptedException e) {
            log.error("CapacityAdjustMessageService.product error:{}",e.getMessage(),e);
        }
    }

    public MoneSpec consume(){
        try {
            return queue.pollFirst();
        } catch (Exception e) {
            log.error("CapacityAdjustMessageService.consume error:{}",e.getMessage(),e);
            return null;
        }
    }

    public int queueSize(){
        return queue.size();
    }

}
