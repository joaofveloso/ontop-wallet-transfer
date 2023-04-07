package com.ontop.balance.infrastructure.configs;

import com.mongodb.WriteConcern;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.WriteConcernResolver;

@Configuration
public class MongoConfiguration {

    @Bean
    public WriteConcernResolver writeConcernResolver() {
        return action -> WriteConcern.MAJORITY;
    }
}
