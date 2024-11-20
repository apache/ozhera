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

package org.apache.ozhera.monitor.service.prometheus;

import org.apache.commons.lang3.StringUtils;

/**
 * @author gaoxihui
 * @date 2021/7/28 10:00 AM
 */
public enum MetricSuffix {
    _total,
    _count,
    _bucket,
    _created;

    public static MetricSuffix getByName(String name) {
        if(StringUtils.isEmpty(name)){
            return null;
        }
        if(MetricSuffix._total.name().equals(name)){
            return MetricSuffix._total;
        }
        if(MetricSuffix._count.name().equals(name)){
            return MetricSuffix._count;
        }
        if(MetricSuffix._bucket.name().equals(name)){
            return MetricSuffix._bucket;
        }
        if(MetricSuffix._created.name().equals(name)){
            return MetricSuffix._created;
        }

        return null;
    }
}
