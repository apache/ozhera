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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

/**
 * Expose Prometheus metrics using a plain Java HttpServer.
 * <p>
 * Example Usage:
 * <pre>
 * {@code
 * HTTPServer server = new HTTPServer(1234);
 * }
 * </pre>
 */
class HTTPServer {
    private static class LocalByteArray extends ThreadLocal<ByteArrayOutputStream> {
        @Override
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(1 << 20);
        }
    }

    /**
     * Handles Metrics collections from the given registry.
     */
    static class HTTPMetricHandler implements HttpHandler {
        private final CollectorRegistry registry;

        private final LocalByteArray buffer = new LocalByteArray();
        private static final String HEALTHY_RESPONSE = "Exporter is Healthy.";

        HTTPMetricHandler(CollectorRegistry registry) {
            this.registry = registry;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                String contextPath = t.getHttpContext().getPath();
                ByteArrayOutputStream response = this.buffer.get();
                response.reset();

                try (OutputStreamWriter osw = new OutputStreamWriter(response, StandardCharsets.UTF_8)) {
                    if ("/-/healthy".equals(contextPath)) {
                        osw.write(HEALTHY_RESPONSE);
                    } else {
                        String contentType = TextFormat.chooseContentType(t.getRequestHeaders().getFirst("Accept"));
                        t.getResponseHeaders().set("Content-Type", contentType);

                        TextFormat.writeFormat(contentType, osw, registry.metricFamilySamples());
                    }
                }

                if (shouldUseCompression(t)) {
                    t.getResponseHeaders().set("Content-Encoding", "gzip");
                    t.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
                    try (GZIPOutputStream os = new GZIPOutputStream(t.getResponseBody())) {
                        response.writeTo(os);
                    }
                } else {
                    t.getResponseHeaders().set("Content-Length",
                            String.valueOf(response.size()));
                    t.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.size());
                    response.writeTo(t.getResponseBody());
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw e;
            } finally {
                t.close();
            }
        }

    }

    protected static boolean shouldUseCompression(HttpExchange exchange) {
        List<String> encodingHeaders = exchange.getRequestHeaders().get("Accept-Encoding");
        if (encodingHeaders == null) return false;

        for (String encodingHeader : encodingHeaders) {
            String[] encodings = encodingHeader.split(",");
            for (String encoding : encodings) {
                if (encoding.trim().equalsIgnoreCase("gzip")) {
                    return true;
                }
            }
        }
        return false;
    }


    static class NamedDaemonThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);

        private final int poolNumber = POOL_NUMBER.getAndIncrement();
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadFactory delegate;
        private final boolean daemon;

        NamedDaemonThreadFactory(ThreadFactory delegate, boolean daemon) {
            this.delegate = delegate;
            this.daemon = daemon;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = delegate.newThread(r);
            t.setName(String.format("prometheus-http-%d-%d", poolNumber, threadNumber.getAndIncrement()));
            t.setDaemon(daemon);
            return t;
        }

        static ThreadFactory defaultThreadFactory(boolean daemon) {
            return new NamedDaemonThreadFactory(Executors.defaultThreadFactory(), daemon);
        }
    }

    protected final HttpServer server;
    protected final ExecutorService executorService;

    /**
     * Start a HTTP server serving Prometheus metrics from the given registry using the given {@link HttpServer}.
     * The {@code httpServer} is expected to already be bound to an address
     */
    HTTPServer(HttpServer httpServer, final CollectorRegistry registry, boolean daemon) {
        if (httpServer.getAddress() == null) {
            throw new IllegalArgumentException("HttpServer hasn't been bound to an address");
        }
        server = httpServer;
        HttpHandler handler = new HTTPMetricHandler(registry);
        server.createContext("/", handler);
        server.createContext("/metrics", handler);
        server.createContext("/-/healthy", handler);
        executorService = Executors.newFixedThreadPool(5, NamedDaemonThreadFactory.defaultThreadFactory(daemon));
        server.setExecutor(executorService);
        start(daemon);
    }

    /**
     * Start a HTTP server by making sure that its background thread inherit proper daemon flag.
     */
    private void start(boolean daemon) {
        if (daemon == Thread.currentThread().isDaemon()) {
            server.start();
        } else {
            FutureTask<Void> startTask = new FutureTask<>(server::start, null);
            NamedDaemonThreadFactory.defaultThreadFactory(daemon).newThread(startTask).start();
            try {
                startTask.get();
            } catch (ExecutionException e) {
                throw new IllegalStateException("Unexpected exception on starting HTTPSever", e);
            } catch (InterruptedException e) {
                // This is possible only if the current tread has been interrupted,
                // but in real use cases this should not happen.
                // In any case, there is nothing to do, except to propagate interrupted flag.
                Thread.currentThread().interrupt();
            }
        }
    }
}

