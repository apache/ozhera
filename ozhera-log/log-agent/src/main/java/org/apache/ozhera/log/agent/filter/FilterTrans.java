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
package org.apache.ozhera.log.agent.filter;

import org.apache.ozhera.log.api.filter.Common;
import org.apache.ozhera.log.api.filter.RateLimitStrategy;
import org.apache.ozhera.log.api.model.meta.FilterConf;
import org.apache.ozhera.log.api.model.meta.FilterDefine;
import org.apache.ozhera.log.api.model.meta.FilterName;

import java.util.Map;

/**
 * Filter packaging conversion class
 */
public class FilterTrans {
    public static FilterConf filterConfTrans(FilterDefine define) {
        if (define == null) {
            return null;
        }
        if (define.getCode() != null) {
            if (define.getCode().startsWith(Common.RATE_LIMIT_CODE)) {
                RateLimitStrategy rateLimiterStrategy = RateLimitStrategy.getRateLimiterStrategy(define.getCode());
                if (rateLimiterStrategy != null) {
                    return consFilterConf(FilterName.RATELIMITER, rateLimiterStrategy, define.getArgs());
                }
            }
        }
        return null;
    }

    private static FilterConf consFilterConf(FilterName name, RateLimitStrategy strategy, Map<String, String> args) {
        return new FilterConf(strategy.getCode(), name, strategy.getType(), strategy.getOrder(), strategy.getLifecycle(), args);
    }
}
