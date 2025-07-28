package com.viescloud.llc.docker_util.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.viescloud.eco.viesspringutils.auto.config.ViesApplicationConfig;
import com.viescloud.eco.viesspringutils.auto.model.authentication.ViesDefaultEndpointEnum;
import com.viescloud.eco.viesspringutils.util.Streams;

@Configuration
public class BeanConfig {
    
    @Bean
    public ViesApplicationConfig viesApplicationConfig(@Value("${spring.profiles.active:local}") String env) {
        var config = new ViesApplicationConfig(env, Streams.stream(ViesDefaultEndpointEnum.values()).map(ViesDefaultEndpointEnum::getEndpoint).toList());
        config.setEnabledHttpClientController(true);
        return config;
    }
}
