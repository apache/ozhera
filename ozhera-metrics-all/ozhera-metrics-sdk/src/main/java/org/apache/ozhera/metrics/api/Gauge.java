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
package org.apache.ozhera.metrics.api;

/**
 * Gauge metric, to report instantaneous values.
 * <p>
 * Examples of Gauge include:
 * <ul>
 *  <li>Inprogress requests</li>
 *  <li>Number of items in a queue</li>
 *  <li>Free memory</li>
 *  <li>Total memory</li>
 *  <li>Temperature</li>
 * </ul>
 *
 * Gauges can go both up and down.
 * <p>
 * An example Gauge:
 * <pre>
 * {@code
 *   class YourClass {
 *
 *     void processRequest() {
 *        Metrics.gauge("inprogress_requests").inc();
 *     }
 *   }
 * }
 * </pre>
 *
 * <p>
 * You can also use labels to track different types of metric:
 * <pre>
 * {@code
 *   class YourClass {
 *
 *     void processGetRequest() {
 *        Metrics.gauge("inprogress_requests").tag("method", "get").inc();
 *        // Your code here.
 *     }
 *     void processPostRequest() {
 *        Metrics.gauge("inprogress_requests").tag("method", "post").inc();
 *        // Your code here.
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * These can be aggregated and processed together much more easily in the Prometheus
 * server than individual metrics for each labelset.
 */
public interface Gauge extends Meter<Gauge> {
    /**
     * Set the gauge to the given value.
     */
    void set(double value);

    /**
     * Increment the gauge by the given amount.
     */
    void inc(double amount);
}
