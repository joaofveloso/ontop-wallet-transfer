package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.balance.core.model.commands.TransferMoneyCommand;
import java.util.Optional;

public interface Transaction {

    Optional<TransactionData> getTransactionsById(String id);

    void starNewTransaction(String transactionId, TransferMoneyCommand command, RecipientData recipientData);

    void addStepToTransaction(String transactionId, String targetSystem, TransactionStatus status);

 ;
}
