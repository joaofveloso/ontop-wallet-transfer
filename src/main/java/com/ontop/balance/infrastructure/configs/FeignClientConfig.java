package com.ontop.balance.infrastructure.configs;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign client configuration for external service calls.
 *
 * <p>Timeouts configured:
 * <ul>
 *   <li>Connect timeout: 5s - time to establish TCP connection</li>
 *   <li>Read timeout: 10s - time to wait for response</li>
 * </ul>
 *
 * <p><b>IMPORTANT:</b> TimeLimiter is configured in resilience4j.properties and must be
 * greater than (connect timeout + read timeout) to avoid premature circuit breaking.
 * Current TimeLimiter setting: 13s (allows Feign timeouts to trigger first).
 */
@Configuration
public class FeignClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options requestOptions() {
        return new Request.Options(
            5, TimeUnit.SECONDS,  // Connect timeout
            10, TimeUnit.SECONDS,  // Read timeout
            true  // Follow redirects
        );
    }
}
