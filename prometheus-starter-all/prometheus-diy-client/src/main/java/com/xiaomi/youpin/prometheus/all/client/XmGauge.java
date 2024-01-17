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
public interface XmGauge {
     /**
      * padding label value
      *
      * @param labelValues
      * @return XMGauge
      */
     XmGauge with(String... labelValues);

     /**
      * Gauge
      *
      * @param delta Increment
      */
     void set(double delta,String... labelValue);

     /**
      * Gauge
      *
      * @param delta set Increment
      */
     void add(double delta,String... labelValue);
}
