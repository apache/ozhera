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
 * Histogram metric, to track distributions of events.
 * <p>
 * Example of uses for Histogram include:
 * <ul>
 *  <li>Response latency</li>
 *  <li>Request size</li>
 * </ul>
 * <p>
 * <em>Note:</em> Each bucket is one timeseries. Many buckets and/or many dimensions with labels
 * can produce large amount of time series, that may cause performance problems.
 *
 * <p>
 * The default buckets are intended to cover a typical web/rpc request from milliseconds to seconds.
 * <p>
 * Example Histogram:
 * <pre>
 * {@code
 *   class YourClass {
 *
 *     void processRequest(Request req) {
 *        long startTime = System.currentTimeMillis();
 *        try {
 *          // Your code here.
 *        } finally {
 *          Metrics.histogram("requests_latency_seconds").observe(System.currentTimeMillis() - startTime);
 *        }
 *     }
 *   }
 * }
 * </pre>
 * <p>
 * You can choose your own buckets:
 * <pre>
 * {@code
 *     Metrics.histogram("requests_latency_seconds").buckets(new double[]{.01, .02, .03, .04});
 * }
 * </pre>
 * offer easy ways to set common bucket patterns.
 */
public interface Histogram extends Meter<Histogram> {
    /**
     * Observe the given amount.
     */
    void observe(double amount);
}
