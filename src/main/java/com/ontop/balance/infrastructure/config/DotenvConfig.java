package com.ontop.balance.infrastructure.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * Configuration class to load environment variables from .env file
 * This ensures that credentials and sensitive configuration are not hardcoded
 */
@Configuration
public class DotenvConfig {

    @PostConstruct
    public void loadEnvVariables() {
        try {
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMissing()
                    .load();

            // Load each environment variable into System properties
            // This allows Spring to access them via ${VAR:default} syntax
            dotenv.entries().forEach(entry -> {
                if (System.getProperty(entry.getKey()) == null) {
                    System.setProperty(entry.getKey(), entry.getValue());
                }
            });
        } catch (Exception e) {
            // Silently fail if .env is missing - Spring will use defaults from application.yaml
            // This is intentional for production environments where env vars are set externally
        }
    }
}
