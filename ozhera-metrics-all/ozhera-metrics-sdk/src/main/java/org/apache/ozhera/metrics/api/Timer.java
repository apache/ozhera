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

import java.util.concurrent.Callable;

/**
 * Timer metric, to track distributions of events.
 *
 * <p>Example of uses for Timer include:
 *
 * <ul>
 *   <li>Request process duration
 * </ul>
 *
 * <p><em>Note:</em> Each bucket is one timeseries. Many buckets and/or many dimensions with labels
 * can produce large amount of time series, that may cause performance problems.
 *
 * <p>The default buckets are intended to cover a typical web/rpc request from milliseconds to
 * seconds.
 *
 * <p>Example Timer:
 *
 * <pre>{@code
 *  class YourClass {
 *
 *    void processRequest(Request req) {
 *       Timer.InnerTimer timer = Metrics.timer("requests_process_duration_seconds").startTimer();
 *       try {
 *         // Your code here.
 *       } finally {
 *         timer.endTimer();
 *       }
 *    }
 *
 *    void recordDurationOfRunnable(Request req) {
 *       Metrics.timer("record_runnable_duration_seconds").record(() -> {
 *           // Your code here.
 *       });
 *    }
 *  }
 * }</pre>
 *
 * <p>You can choose your own buckets:
 *
 * <pre>{@code
 * Metrics.timer("requests_process_duration_seconds").buckets(new double[]{.01, .02, .03, .04});
 * }</pre>
 *
 * offer easy ways to set common bucket patterns.
 */
public interface Timer extends Meter<Timer> {
    /**
     * Start timer
     */
    InnerTimer startTimer();

    interface InnerTimer {
        /**
         * end timer
         * @return duration
         */
        double endTimer();
    }

    void record(double duration);

    double record(Runnable runnable);

    <E> E record(Callable<E> callable);
}
