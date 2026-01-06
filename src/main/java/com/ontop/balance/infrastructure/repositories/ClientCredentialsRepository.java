package com.ontop.balance.infrastructure.repositories;

import com.ontop.balance.infrastructure.entities.ClientCredentialsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientCredentialsRepository extends MongoRepository<ClientCredentialsEntity, Long> {
    Optional<ClientCredentialsEntity> findByClientIdAndActiveTrue(Long clientId);
}
