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
package org.apache.ozhera.log.manager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/2/26 17:03
 */
public class TestHttpClient {

    public static void main(String[] args) {
        // 创建HTTP客户端并配置连接池
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(4);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(2000).setConnectionRequestTimeout(2000).setSocketTimeout(4000).build();
        HttpRequestRetryHandler retryHandler = new StandardHttpRequestRetryHandler();
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).setRetryHandler(retryHandler).build();

        // 创建线程池
        ExecutorService executorService = Executors.newFixedThreadPool(20);

        try {
            for (int i = 0; i < 100; i++) {
                // 提交任务给线程池
                executorService.execute(() -> {
                    // 创建GET请求
                    HttpGet httpGet = new HttpGet("http://localhost:9905/nacos/#/configeditor?serverId=center&dataId=hera_log_manager_open&group=DEFAULT_GROUP&namespace=&edasAppName=&edasAppId=&searchDataId=&searchGroup=");

                    try {
                        // 执行请求
                        CloseableHttpResponse response = httpClient.execute(httpGet);
                        try {
                            // 获取响应实体
                            HttpEntity entity = response.getEntity();
                            if (entity != null) {
                                // 处理响应内容，这里简单打印响应的状态码和内容
                                System.out.println("Response status: " + response.getStatusLine());
                                System.out.println("Response content: " + entity.getContent());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (null != response) {
                                    response.close();
                                }
                            } catch (IOException var19) {
                                var19.printStackTrace();
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } finally {
            // 关闭线程池
            executorService.shutdown();
        }
    }
}
