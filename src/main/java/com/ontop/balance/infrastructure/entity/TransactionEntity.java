package com.ontop.balance.infrastructure.entity;

import java.time.LocalDateTime;
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

    public TransactionEntity(String id, Long clientId, String recipientId, String recipientName) {
        this.id = id;
        this.clientId = clientId;
        this.recipientId = recipientId;
        this.recipientName = recipientName;
        this.createdAt = LocalDateTime.now();
    }
}
