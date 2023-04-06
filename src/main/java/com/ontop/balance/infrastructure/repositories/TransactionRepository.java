package com.ontop.balance.infrastructure.repositories;

import com.ontop.balance.infrastructure.entity.TransactionEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<TransactionEntity, String> {

}
