package com.ontop.balance.infrastructure.configs;

import com.ontop.balance.infrastructure.interceptors.FeignInterceptor;
import feign.InvocationContext;
import feign.RequestInterceptor;
import feign.ResponseInterceptor;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignInterceptor();
    }
}
