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
