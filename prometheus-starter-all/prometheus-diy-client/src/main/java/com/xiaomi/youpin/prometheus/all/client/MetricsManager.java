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
package com.xiaomi.youpin.prometheus.all.client;
/**
 * @author zhangxiaowei6
 */
public interface MetricsManager {
     /**
      * use Counter
      *
      * @param metricName
      * @param labelNames
      * @return XMCounter
      */

     XmCounter newCounter(String metricName, String... labelNames);

     /**
      * use Gauge
      *
      * @param metricName
      * @param labelNames
      * @return XMGauge
      */

     XmGauge newGauge(String metricName, String... labelNames);

     /**
      * use Histogram
      *
      * @param metricName
      * @param buckets
      * @param labelNames
      * @return XMHistogram
      */

     XmHistogram newHistogram(String metricName, double[] buckets, String... labelNames);
}
