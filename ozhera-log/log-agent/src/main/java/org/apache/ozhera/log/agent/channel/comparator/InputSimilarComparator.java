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
package org.apache.ozhera.log.agent.channel.comparator;

import org.apache.ozhera.log.agent.input.Input;
import lombok.extern.slf4j.Slf4j;

import static org.apache.ozhera.log.common.Constant.GSON;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2021/9/15 10:08
 */
@Slf4j
public class InputSimilarComparator implements SimilarComparator<Input> {

    private Input oldInput;

    public InputSimilarComparator(Input oldInput) {
        this.oldInput = oldInput;
    }

    @Override
    public boolean compare(Input newInput) {
        if (null == oldInput) {
            return false;
        }
        if (oldInput == newInput) {
            return true;
        }
        return baseSimilarCompare(newInput);
    }

    private boolean baseSimilarCompare(Input newInput) {
        try {
            if (oldInput.equals(newInput)) {
                return true;
            }
        } catch (Exception e) {
            log.error("input compare error:new input:{},oldInput:{}", GSON.toJson(newInput), GSON.toJson(oldInput), e);
        }
        return false;
    }


}
