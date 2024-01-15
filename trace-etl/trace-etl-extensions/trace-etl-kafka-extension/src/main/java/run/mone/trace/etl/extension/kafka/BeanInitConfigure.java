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
package run.mone.trace.etl.extension.kafka;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanInitConfigure {

    @NacosValue("${kafka.vpc.type}")
    private String vpcType;

    @Bean
    public KafkaConfigure getKafkaConfigure(){
        if(VpcType.VPC_SSL_9003.equals(vpcType)){
            return new KafkaConfigure9093();
        }else if(VpcType.VPC_9004.equals(vpcType)){
            return new KafkaConfigure9094();
        }else if(VpcType.VPC_9002.equals(vpcType)){
            return new KafkaConfigure9092();
        }else{
            throw new RuntimeException("vpcType is unknow!");
        }
    }
}
