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
package com.xiaomi.youpin.prometheus.agent.param.scrapeConfig;

import com.xiaomi.youpin.prometheus.agent.param.prometheus.*;
import lombok.Data;
import lombok.ToString;
import java.util.Map;
import java.util.List;

@Data
@ToString(callSuper = true)
public class ScrapeConfigDetail {

    private String job_name;
    private String scrape_interval;
    private String scrape_timeout;
    private String metrics_path;
    private boolean honor_labels;
    private boolean honor_timestamps;
    private String scheme;
    private Map<String, List<String>> params;
    private BasicAuth basic_auth;
    private List<Static_configs> static_configs;
    private List<Relabel_configs> relabel_configs;
    private List<Http_sd_configs> http_sd_configs;
    private List<Metric_relabel_configs> metric_relabel_configs;

}
