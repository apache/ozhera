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
package com.xiaomi.youpin.prometheus.agent.param.prometheus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Oauth2 {

    private String client_id;
    private String client_secret;
    private String token_url;
    private Tls_config tls_config;
    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }
    public String getClient_id() {
        return client_id;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }
    public String getClient_secret() {
        return client_secret;
    }

    public void setToken_url(String token_url) {
        this.token_url = token_url;
    }
    public String getToken_url() {
        return token_url;
    }

    public void setTls_config(Tls_config tls_config) {
        this.tls_config = tls_config;
    }
    public Tls_config getTls_config() {
        return tls_config;
    }

}