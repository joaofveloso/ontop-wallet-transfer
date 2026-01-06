package com.ontop.balance.infrastructure.configs;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;

@TestConfiguration
public class MongoTestContainerConfig {

    @Bean(destroyMethod = "stop")
    @Primary
    public MongoDBContainer mongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer("mongo:6.0.9");
        container.start();
        return container;
    }

    @Bean(destroyMethod = "close")
    @Primary
    public MongoClient mongoClient(MongoDBContainer mongoDBContainer) {
        String connectionString = mongoDBContainer.getReplicaSetUrl();
        return MongoClients.create(connectionString);
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(new SimpleMongoClientDatabaseFactory(
            mongoClient,
            "test"
        ));
    }
}
