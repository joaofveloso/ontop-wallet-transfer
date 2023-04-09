package com.ontop.balance.core.model;

import com.ontop.balance.core.model.exceptions.UnauthorizedAccessToResourceException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public record TransactionData(String transactionId, Long clientId, LocalDateTime createdAt,
                              List<TransactionItemData> steps) {

    public record TransactionItemData(LocalDateTime createdAt, String targetSystem,
                                      TransactionStatus status) {

    }

    public void validateOwnership(Long clientId) {
        if (!Objects.equals(this.clientId, clientId)) {
            throw new UnauthorizedAccessToResourceException();
        }
    }

    public enum TransactionStatus {
        PENDING("Pending"), IN_PROGRESS("In Progress"), COMPLETED("Completed"), FAILED(
                "Failed"), CANCELED("Canceled");

        private final String status;

        TransactionStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }
}
