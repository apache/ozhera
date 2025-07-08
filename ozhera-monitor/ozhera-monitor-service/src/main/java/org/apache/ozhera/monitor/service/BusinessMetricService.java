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
package org.apache.ozhera.monitor.service;

import org.apache.ozhera.monitor.result.Result;
import org.apache.ozhera.monitor.service.model.PageData;

import java.util.Map;

public interface BusinessMetricService {

    Result<PageData> queryRangeSumOverTime(String metric_, Map labels, String metricSuffix, Long startTime, Long endTime, Long step, String duration, String sumBy);

    Result<PageData> queryRange(String metric_, Map labels, String projectName, String metricSuffix, Long startTime, Long endTime, Long step, String op, double value);

    String completePromQL(String source, Map labels, String metricSuffix, String op, double value, String duration, String offset);
}
