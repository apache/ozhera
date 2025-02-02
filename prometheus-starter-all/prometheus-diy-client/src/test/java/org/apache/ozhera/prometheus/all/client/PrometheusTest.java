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
import org.junit.Before;
import org.junit.Test;

import java.util.stream.IntStream;

public class PrometheusTest {

    @Before
    public void setUp() throws Exception {

        //HTTPServer newServer = new HTTPServer(8070);
        /*try {
            Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Sets.newHashSet());
            StringWriter writer = new StringWriter();
            String str = "";
            try {
                TextFormat.write004(writer, samples);
                StringBuffer sb = writer.getBuffer();
                str = sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(str);
            HttpServer server = HttpServer.create(new InetSocketAddress(8070), 0);
            server.createContext("/metrics", httpExchange -> {
                String response = Prometheus.registry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            new Thread(server::start).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //m = new Metrics("systech", "zxw_test2");*/
        Metrics.getInstance().init("systch","zxw_test2");
    }

    @Test
    public void testCounter() {
        IntStream.range(0, 100).forEach(i -> {

            Metrics.getInstance().newCounter("testCounter", "name").
                    with("zxw").
                    add(1,"zxw");
            Metrics.getInstance().newCounter("testCounter2", "age", "city").with("99", "china").add(1,"18","beijing");
        });
    }

    @Test
    public void testGauge()  {
        IntStream.range(0, 100).forEach(i -> {
            Metrics.getInstance().newGauge("testGauge", "name").with("zxw").set(128,"zxw");
        });
    }

    @Test
    public void testHistogram() {
        IntStream.range(0,100).forEach(i-> {
            Metrics.getInstance().newHistogram("testHistogramWithDefaultBucket",null,"name").with("zxw").observe(12,"zxw");
            Metrics.getInstance().newHistogram("testHistogramWithDiyBucket", new double[] {.01, .05, 0.7, 5 ,10, 50, 100, 200},"name").with("zxw").observe(12,"zxw");
        });
    }

/*    @Test
    public void testException()   {
        IntStream.range(0, 100).forEach(i -> {
            Metrics.getInstance().newCounter("testNotMatchLabelNameAndLabelValueExceptionCounter", "a", "b").with("1").add(1);
            Metrics.getInstance().newGauge("testNotMatchLabelNameAndLabelValueExceptionGauge", "a", "b").with("1").add(1);
            Metrics.getInstance().newHistogram("testNotMatchLabelNameAndLabelValueExceptionHistogram", null, "b", "c").with("1").observe(1);
        });
    }*/
/*    @Test
    public void testJvm() throws InterruptedException {
        Thread.sleep(3000000);
    }*/
}