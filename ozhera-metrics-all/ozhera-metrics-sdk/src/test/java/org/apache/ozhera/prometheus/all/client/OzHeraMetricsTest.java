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

package org.apache.ozhera.prometheus.all.client;

import org.apache.ozhera.metrics.api.BusinessMetrics;
import org.apache.ozhera.metrics.api.Metrics;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Metrics unit test class
 * Test functionality of the new Metrics API
 */
public class OzHeraMetricsTest {

    @Before
    public void setUp() throws Exception {
        // New version Metrics uses static methods, no need to initialize singleton
        // Metrics class has completed initialization work in static code block
        System.out.println("Metrics test environment setup completed");
    }

    /**
     * Test Counter metric
     * Counter is used for cumulative counting, can only increase but not decrease
     */
    @Test
    public void testCounter() {
        IntStream.range(0, 100).forEach(i -> {
            // Test basic Counter - with single tag
            Metrics.counter("testCounter")
                    .tag("name", "zxw")  // Use tag method to set label
                    .inc(1);  // Increment count

            // Test multi-tag Counter
            Metrics.counter("testCounter2")
                    .tag("age", "18")
                    .tag("city", "beijing")
                    .inc(1);
        });
    }

    /**
     * Test Gauge metric
     * Gauge is used to measure instantaneous values, can increase or decrease arbitrarily
     */
    @Test
    public void testGauge() {
        IntStream.range(0, 100).forEach(i -> {
            // Test basic Gauge
            Metrics.gauge("testGauge")
                    .tag("name", "zxw")
                    .set(128);  // Set current value
        });
    }

    /**
     * Test Histogram metric
     * Histogram is used to measure distributions, such as response time distribution
     */
    @Test
    public void testHistogram() {
        IntStream.range(0, 100).forEach(i -> {
            // Histogram using default buckets
            Metrics.histogram("testHistogramWithDefaultBucket")
                    .tag("name", "zxw")
                    .observe(12);  // Record observation value

            // Histogram using custom buckets
            double[] customBuckets = {.01, .05, 0.7, 5, 10, 50, 100, 200};
            Metrics.histogram("testHistogramWithDiyBucket", customBuckets)
                    .tag("name", "zxw")
                    .observe(12);
        });
    }

    /**
     * Test Timer metric
     * Timer is used to measure duration
     */
    @Test
    public void testTimer() {
        IntStream.range(0, 10).forEach(i -> {
            // Test timer functionality
            var timer = Metrics.timer("testTimer")
                    .tag("operation", "test")
                    .startTimer();
            
            try {
                // Simulate some processing time
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                // End timing
                timer.endTimer();
            }

            // Test directly recording time
            Metrics.timer("testTimerRecord")
                    .tag("operation", "direct")
                    .record(0.1);  // Record 100ms

            // Test using Runnable approach
            Metrics.timer("testTimerRunnable")
                    .tag("operation", "runnable")
                    .record(() -> {
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
        });
    }

    /**
     * Test chained operations and multiple tags
     */
    @Test
    public void testChainedOperations() {
        // Test chained tag setting
        Metrics.counter("chainedCounter")
                .tag("service", "order-service")
                .tag("method", "POST")
                .tag("endpoint", "/api/orders")
                .inc(1);

        // Test using tags method to set multiple tags in batch
        Metrics.gauge("batchTagsGauge")
                .tags("region", "beijing", "zone", "zone-a", "instance", "instance-1")
                .set(99.9);
    }

    /**
     * Test exception handling
     * Verify validity of metric names and tags
     */
    @Test
    public void testExceptionHandling() {
        try {
            // Test normal case
            Metrics.counter("validCounter")
                    .tag("env", "test")
                    .inc(1);
            
            System.out.println("Normal metric creation successful");
        } catch (Exception e) {
            System.err.println("Metric creation failed: " + e.getMessage());
        }
    }

    /**
     * Test business metrics
     * BusinessMetrics provides higher-level business scenario metric encapsulation
     */
    @Test
    public void testBusinessMetrics() {
        // Test business Counter metric
        BusinessMetrics.counter("order_scene", "order_count", 1.0);
        
        // Test business Counter metric with context
        Map<String, String> context = new HashMap<>();
        context.put("region", "beijing");
        context.put("channel", "mobile");
        BusinessMetrics.counter("payment_scene", "payment_amount", 100.5, context);
        
        // Test business Gauge metric
        BusinessMetrics.gauge("inventory_scene", "stock_level", 500.0);
        
        // Test business Gauge metric with context
        Map<String, String> stockContext = new HashMap<>();
        stockContext.put("warehouse", "beijing_warehouse");
        stockContext.put("product_type", "electronics");
        BusinessMetrics.gauge("warehouse_scene", "available_stock", 1000.0, stockContext);
    }

    /*
     * Commented methods retain original logic for reference
     * These are usage patterns of the old version API
     */
    
    /*
    @Test
    public void testNotMatchLabelNameAndLabelValueException() {
        IntStream.range(0, 100).forEach(i -> {
            // Old version label mismatch test
            Metrics.getInstance().newCounter("testNotMatchLabelNameAndLabelValueExceptionCounter", "a", "b").with("1").add(1);
            Metrics.getInstance().newGauge("testNotMatchLabelNameAndLabelValueExceptionGauge", "a", "b").with("1").add(1);
            Metrics.getInstance().newHistogram("testNotMatchLabelNameAndLabelValueExceptionHistogram", null, "b", "c").with("1").observe(1);
        });
    }
    */
    
    /*
    @Test
    public void testJvm() throws InterruptedException {
        // JVM metrics monitoring test
        Thread.sleep(3000000);
    }
    */
}