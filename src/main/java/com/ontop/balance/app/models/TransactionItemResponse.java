package com.ontop.balance.app.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "A transaction item response")
public record TransactionItemResponse(
        @Schema(description = "The transaction ID", example = "12345") String transactionId,
        @Schema(description = "The date and time the transaction was created") LocalDateTime createdAt,
        @Schema(description = "A list of transaction steps") List<TransactionStepResponse> steps) {

    @Schema(description = "A transaction step response")
    public record TransactionStepResponse(
            @Schema(description = "The date and time the transaction step was created") LocalDateTime createdAt,
            @Schema(description = "The target system of the transaction step", example = "PaymentAdapter") String targetSystem,
            @Schema(description = "The status of the transaction step", example = "IN_PROGRESS") String status) {

    }
}
