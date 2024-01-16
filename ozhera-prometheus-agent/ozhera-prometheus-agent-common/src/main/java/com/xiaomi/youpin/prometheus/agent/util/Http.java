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
package com.xiaomi.youpin.prometheus.agent.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
public class Http {
    public static String innerRequest(String data, String url, String method) {
        try {
            URL requestUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
            PrintWriter out = null;
            // Setting parameters and regular request properties for URLConnection.
            conn.setRequestProperty("Expect", "");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            //conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(method);
            conn.connect();
            if ("POST".equals(method)) {
                // POST request
                BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                out1.write(data);
                out1.flush();
                out1.close();
            }
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String finalStr = "";
            String str = "";
            while ((str = br.readLine()) != null) {
                finalStr = new String(str.getBytes(), "UTF-8");
            }
            is.close();
            int responseCode = conn.getResponseCode();
            conn.disconnect();
            log.info("innerRequest param url:{},method:{},responseCode:{}", url, method, responseCode);
            return String.valueOf(responseCode);
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static String innerRequestResponseData(String data, String url, String method) {
        try {
            URL requestUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
            PrintWriter out = null;
            // Setting parameters and regular request properties for URLConnection.
            conn.setRequestProperty("Expect", "");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            //conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(method);
            conn.connect();
            if ("POST".equals(method)) {
                // POST request
                BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                out1.write(data);
                out1.flush();
                out1.close();
            }
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String finalStr = "";
            String str = "";
            while ((str = br.readLine()) != null) {
                finalStr = new String(str.getBytes(), "UTF-8");
            }
            is.close();
            conn.disconnect();
            log.info("innerRequestResponseData param url:{},method:{}", url, method);
            return finalStr;
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
