package com.andah.userservice.config;

import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
public class EurekaConfig {

    /**
     * This bean configures additional Eureka instance properties
     * particularly useful in containerized environments
     */
    @Bean
    public EurekaInstanceConfigBean eurekaInstanceConfig(InetUtils inetUtils, Environment environment) {
        EurekaInstanceConfigBean config = new EurekaInstanceConfigBean(inetUtils);
        
        String hostname;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "localhost";
        }
        
        config.setNonSecurePort(environment.getProperty("server.port", Integer.class, 8081));
        config.setHostname(hostname);
        config.setPreferIpAddress(true);
        config.setInstanceId(hostname + ":" + environment.getProperty("spring.application.name") + ":" + 
                             environment.getProperty("server.port", "8081"));
        
        return config;
    }
}
