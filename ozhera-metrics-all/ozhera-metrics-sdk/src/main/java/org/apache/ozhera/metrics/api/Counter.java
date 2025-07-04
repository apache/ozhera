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
 * Counter metric, to track counts of events or running totals.
 * <p>
 * Example of Counter include:
 * <ul>
 *  <li>Number of requests processed</li>
 *  <li>Number of items that were inserted into a queue</li>
 *  <li>Total amount of data a system has processed</li>
 * </ul>
 *
 * Counters can only go up (and be reset), if your use case can go down you should use a {@link Gauge} instead.
 * Use the <code>rate()</code> function in Prometheus to calculate the rate of increase of a Counter.
 * By convention, the names of Counter are suffixed by <code>_total</code>.
 *
 * <p>
 * An example Counter:
 * <pre>
 * {@code
 *   class YourClass {
 *
 *     void processRequest() {
 *        Metrics.counter("requests_total").inc();
 *        try {
 *          // Your code here.
 *        } catch (Exception e) {
 *          Metrics.counter("requests_failed_total").inc();
 *          throw e;
 *        }
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
 *        Metrics.count("requests_total").tag("method", "get").inc();
 *        // Your code here.
 *     }
 *     void processPostRequest() {
 *        Metrics.count("requests_total").tag("method", "post").inc();
 *        // Your code here.
 *     }
 *   }
 * }
 * </pre>
 * These can be aggregated and processed together much more easily in the Prometheus
 * server than individual metrics for each labelset.
 *
 * If there is a suffix of <code>_total</code> on the metric name, it will be
 * removed. When exposing the time series for counter value, a
 * <code>_total</code> suffix will be added. This is for compatibility between
 * OpenMetrics and the Prometheus text format, as OpenMetrics requires the
 * <code>_total</code> suffix.
 */
public interface Counter extends Meter<Counter> {
    /**
     * Increment the counter by 1.
     */
    void inc();

    /**
     * Increment the counter by the given amount.
     * @throws IllegalArgumentException If amount is negative.
     */
    void inc(double amount);
}
