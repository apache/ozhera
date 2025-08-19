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
package org.apache.ozhera.metrics.config;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2025/5/12 16:16
 */
public class LogFileNameUtil {
    public static final String LOG_PATH_PROPERTY_NAME = "otel.exporter.log.pathprefix";
    private static final String LOG_PATH_SUFFIX = "/metrics/";
    private static final String LOG_FILE_NAME = "custom-metrics.log";

    public static String getLogPathFile() {
        return getLogPath() + LOG_FILE_NAME;
    }

    public static String getLogPath() {
        String logPathPrefixStr = System.getenv("MIONE_LOG_PATH");
        if (logPathPrefixStr == null || logPathPrefixStr.isEmpty()) {
            String logPathPrefix = System.getProperty(LOG_PATH_PROPERTY_NAME);
            if (logPathPrefix == null || logPathPrefix.isEmpty()) {
                logPathPrefix = "/home/work/log/";
            }
            String applicationName = getServiceName();
            logPathPrefixStr = logPathPrefix + applicationName;
        }
        return logPathPrefixStr + LOG_PATH_SUFFIX;
    }

    /**
     * get service name without project id
     */
    public static String getServiceName() {
        String applicationName = System.getProperty("otel.resource.attributes") == null ?
                (System.getenv("mione.app.name") == null ? "none" : System.getenv("mione.app.name")) :
                System.getProperty("otel.resource.attributes").split("=")[1];

        int i = applicationName.indexOf("-");
        if (i >= 0) {
            String id = applicationName.substring(0, i);
            if (isNumeric(id)) {
                return applicationName.substring(i + 1);
            }
        }
        int j = applicationName.indexOf("_");
        if (j >= 0) {
            String id = applicationName.substring(0, j);
            if (isNumeric(id)) {
                return applicationName.substring(j + 1);
            }
        }
        return applicationName;
    }

    public static boolean isNumeric(String cs) {
        if (cs == null || cs.isEmpty()) {
            return false;
        }
        int sz = cs.length();
        for (int i = 0; i < sz; i++) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
