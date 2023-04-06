package com.ontop.balance.infrastructure.messages;

import com.fasterxml.jackson.databind.JsonSerializer;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferMessage {
    private Long clientId;
    private String recipientId;
    private String name;
    private String routingNumber;
    private String nationalIdentification;
    private String accountNumber;
    private BigDecimal amount;
    private String transactionId;
}