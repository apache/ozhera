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
package com.xiaomi.mione.prometheus.starter.all.service;

public abstract class PrometheusService {

    protected static final String DEFAULT_SERVICE_NAME = "default_service_name";

    protected static final String DEFAULT_PORT = "5555";

    public abstract String getServiceName();

    public abstract String getServerIp();

    public abstract String getPort();
}
