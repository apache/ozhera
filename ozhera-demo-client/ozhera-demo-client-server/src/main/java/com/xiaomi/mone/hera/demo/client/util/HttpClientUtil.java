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
package com.xiaomi.mone.hera.demo.client.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

public class HttpClientUtil {

	private PoolingHttpClientConnectionManager pool = null;
	private int TIMEOUT = 5 * 1000;
	private int MAX_HTTP_TOTAL_CONNECTION = 1000;
	private int MAX_CONNECTION_PER_HOST = 200;
	private RequestConfig requestConfig = null;
	private CloseableHttpClient singleHttpClient = null;

	private HttpClientUtil() {
		requestConfig = RequestConfig.custom()
				.setSocketTimeout(TIMEOUT)
				.setConnectTimeout(TIMEOUT)
				.setConnectionRequestTimeout(TIMEOUT)
				.build();

		//
		ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {
			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				return 20 * 1000;
			}
		};
		//
		pool = new PoolingHttpClientConnectionManager();
		pool.setMaxTotal(MAX_HTTP_TOTAL_CONNECTION);
		pool.setDefaultMaxPerRoute(MAX_CONNECTION_PER_HOST);
		singleHttpClient = HttpClients.custom().setConnectionManager(pool)
				.setKeepAliveStrategy(keepAliveStrategy)
				.setMaxConnTotal(MAX_HTTP_TOTAL_CONNECTION)
				.setMaxConnPerRoute(MAX_CONNECTION_PER_HOST)
				.setDefaultRequestConfig(requestConfig)
				.setUserAgent("Mozilla/4.0")
				.build();
	}

	/**
	 * Get HttpClient instance
	 *
	 * @return
	 */
	public static HttpClientUtil getInstance() {
		return HttpUtilSingle.instance;
	}

	/**
	 * Obtain a CloseableHttpClient from the thread pool.
	 *
	 * @return
	 */
	public CloseableHttpClient getHttpClient() {

		return singleHttpClient;
	}

	/**
	 * Release CloseableHttpResponse
	 * Print detailed call logs
	 *
	 * @param hur
	 * @param chr
	 * @param startTime
	 */
	public static void closeResponse(HttpUriRequest hur, CloseableHttpResponse chr, long startTime) {
		String url = "http://nourl.com";
		int stateCode = -1;
		long endTime = System.currentTimeMillis();
		long useTime = endTime - startTime;
		try {

			if (null != hur) {
				url = hur.getURI().getPath();
			}

			if (null != chr) {
				stateCode = chr.getStatusLine().getStatusCode();
				chr.close();
			}
		} catch (Exception e) {

		}

	}

	/**
	 * Get the default RequestConfig
	 *
	 * @return
	 */
	public RequestConfig getRequestConfig() {

		return requestConfig;
	}

	/**
	 * Singleton pattern
	 */
	private static class HttpUtilSingle {
		private static HttpClientUtil instance = new HttpClientUtil();
	}

	/**
	 * Get current time
	 *
	 * @return
	 */
	public static long currentTimeMillis() {

		return System.currentTimeMillis();
	}
}
