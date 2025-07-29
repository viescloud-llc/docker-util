package com.viescloud.llc.docker_util.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.viescloud.eco.viesspringutils.auto.config.ViesApplicationConfig;
import com.viescloud.eco.viesspringutils.auto.model.authentication.ViesDefaultEndpointEnum;
import com.viescloud.eco.viesspringutils.util.Streams;

import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class BeanConfig {
    
    @Bean
    public ViesApplicationConfig viesApplicationConfig(@Value("${spring.profiles.active:local}") String env) {
        var config = new ViesApplicationConfig(env, Streams.stream(ViesDefaultEndpointEnum.values()).map(ViesDefaultEndpointEnum::getEndpoint).toList());
        config.setEnabledHttpClientController(true);
        return config;
    }

    @Bean
    public CorsWebFilter corsWebFilter() {
        final CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Collections.singletonList("*"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList("*"));
        corsConfig.addAllowedHeader("*");
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsWebFilter(source);
    }

    @Bean
    public WebMvcConfigurer CORSConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(org.springframework.web.servlet.config.annotation.CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedHeaders("*")
                        .allowedMethods("*");
            }
        };
    }
}
