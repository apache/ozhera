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
package com.xiaomi.mione.prometheus.starter.all.factory;

import com.xiaomi.mione.prometheus.starter.all.service.MilinePrometheusService;
import com.xiaomi.mione.prometheus.starter.all.service.PrometheusService;
import org.apache.commons.lang3.StringUtils;

import com.xiaomi.mione.prometheus.starter.all.domain.Const ;


/**
 * @Description
 * @Author dingtao
 * @Date 2023/3/5 3:33 PM
 */
public class ServiceFactory {

    public static PrometheusService getPrometheusService(String platform){
        if(StringUtils.isEmpty(platform)){
            throw new IllegalArgumentException("platform is empty");
        }
        switch (platform) {
            case Const.MILINE:
                return new MilinePrometheusService();
            default:
                throw new IllegalArgumentException("platform is invalid");
        }
    }
}
