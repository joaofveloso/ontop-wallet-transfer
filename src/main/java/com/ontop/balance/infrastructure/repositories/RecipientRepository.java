package com.ontop.balance.infrastructure.repositories;

import com.ontop.balance.infrastructure.entity.RecipientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RecipientRepository extends MongoRepository<RecipientEntity, String> {

    Page<RecipientEntity> findAllByClientId(Long clientId, Pageable pageable);
}
