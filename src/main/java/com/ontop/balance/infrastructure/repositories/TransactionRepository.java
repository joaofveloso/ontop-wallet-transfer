package com.ontop.balance.infrastructure.repositories;

import com.ontop.balance.infrastructure.entities.TransactionEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TransactionRepository extends MongoRepository<TransactionEntity, String> {

    Page<TransactionEntity> findAllByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    Page<TransactionEntity> findByClientIdAndCreatedAtBetween(Long clientId, LocalDateTime startDate,
            LocalDateTime endDate, Pageable page);

    default Page<TransactionEntity> findByClientIdAndCreatedAtBetween(Long clientId, LocalDate startDate,
            Pageable page) {
        return startDate == null ? findAllByClientIdOrderByCreatedAtDesc(clientId, page)
                : this.findByClientIdAndCreatedAtBetween(clientId, startDate.atStartOfDay(),
                        startDate.plusDays(1).atStartOfDay(), page);
    }
}
