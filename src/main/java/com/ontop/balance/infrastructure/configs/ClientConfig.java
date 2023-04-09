package com.ontop.balance.infrastructure.configs;

import com.ontop.balance.infrastructure.interceptors.FeignInterceptor;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignInterceptor();
    }
}
