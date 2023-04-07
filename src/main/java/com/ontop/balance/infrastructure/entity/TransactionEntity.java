package com.ontop.balance.infrastructure.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document("transactions")
@NoArgsConstructor
public class TransactionEntity {

    @Id
    private String id;
    private Long clientId;
    private String recipientId;
    private String recipientName;
    private LocalDateTime createdAt;
    private BigDecimal amount;

    private List<TransactionItem> steps;

    public TransactionEntity(String id, Long clientId, String recipientId, String recipientName, BigDecimal amount) {
        this.id = id;
        this.clientId = clientId;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.steps = new ArrayList<>();
    }

    @Getter
    @NoArgsConstructor
    public static class TransactionItem {

        private LocalDateTime createdAt;
        private String targetSystem;
        private String status;

        public TransactionItem(String targetSystem, String status) {
            this.createdAt = LocalDateTime.now();
            this.targetSystem = targetSystem;
            this.status = status;
        }
    }
}
