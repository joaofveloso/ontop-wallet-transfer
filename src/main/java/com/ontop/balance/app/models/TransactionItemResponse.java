package com.ontop.balance.app.models;

import java.time.LocalDateTime;
import java.util.List;

public record TransactionItemResponse(
        String transactionId, LocalDateTime createdAt, List<TransactionStepResponse> steps) {
    public record TransactionStepResponse(LocalDateTime createdAt, String targetSystem, String status){}
}
