package run.mone.trace.etl.extension.doris.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.mone.doris.DorisService;

@Configuration
public class DorisConfig {

    @Value("${doris.driver}")
    private String driver;
    @Value("${doris.url}")
    private String url;
    @NacosValue("${doris.username}")
    private String username;
    @NacosValue("${doris.password}")
    private String password;


    @Bean
    public DorisService getDorisService(){
        return new DorisService(driver, url, username, password);
    }
}
