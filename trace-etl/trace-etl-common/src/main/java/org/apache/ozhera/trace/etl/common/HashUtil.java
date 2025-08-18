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

package org.apache.ozhera.trace.etl.common;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

/**
 * @Description
 * @Date 2022/11/6 4:42 pm
 */
public class HashUtil {

    public static int consistentHash(String traceId, int buckets) {
        HashCode hasCode = Hashing.murmur3_32().hashString(traceId, StandardCharsets.UTF_8);
        return Hashing.consistentHash(hasCode, buckets);
    }
}