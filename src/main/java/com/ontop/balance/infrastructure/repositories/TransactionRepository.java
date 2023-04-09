package com.ontop.balance.infrastructure.repositories;

import com.ontop.balance.infrastructure.entities.TransactionEntity;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface TransactionRepository extends MongoRepository<TransactionEntity, String> {

    Page<TransactionEntity> findAllByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    @Query("{ 'clientId' : ?0, 'createAt' : { $gte: ?1, $lt: ?2 }}")
    Page<TransactionEntity> findDocumentsByCreateDate(Long clientId, LocalDate startDate,
            LocalDate endDate, Pageable page);

    default Page<TransactionEntity> findDocumentsByCreateDate(Long clientId, LocalDate startDate,
            Pageable page) {
        return startDate == null ? findAllByClientIdOrderByCreatedAtDesc(clientId, page)
                : this.findDocumentsByCreateDate(clientId, startDate, startDate.plusDays(1), page);
    }
}
