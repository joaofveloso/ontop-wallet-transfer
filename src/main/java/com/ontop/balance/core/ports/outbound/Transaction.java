package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import java.util.Optional;

public interface Transaction {

    Optional<TransactionData> getTransactionsById(String id);

    void starNewTransaction(String transactionId, Long clientId, String id, String name);

    void addStepToTransaction(String transactionId, String targetSystem, TransactionStatus status);
}
