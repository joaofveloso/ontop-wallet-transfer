package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.TransactionData.TransactionItemData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.balance.core.ports.outbound.Transaction;
import com.ontop.balance.infrastructure.entity.TransactionEntity;
import com.ontop.balance.infrastructure.entity.TransactionEntity.TransactionItem;
import com.ontop.balance.infrastructure.repositories.TransactionRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionAdapter implements Transaction {

    private final TransactionRepository transactionRepository;

    @Override
    public Optional<TransactionData> getTransactionsById(String id) {
        return this.transactionRepository.findById(id).map(this::toTransactionData);
    }

    public TransactionData toTransactionData(TransactionEntity entity) {

        List<TransactionItemData> transactionItemData = entity.getSteps().stream()
                .map(step -> new TransactionItemData(step.getCreatedAt(),
                        step.getTargetSystem(), TransactionStatus.valueOf(step.getStatus())))
                .toList();
        return new TransactionData(
                entity.getId(), entity.getClientId(), entity.getCreatedAt(), transactionItemData);
    }

    @Override
    public void starNewTransaction(String transactionId, Long clientId, String recipientId, String name) {

        TransactionEntity transactionEntity = new TransactionEntity(transactionId, clientId, recipientId,
                name);
        this.transactionRepository.save(transactionEntity);
    }

    @Override
    public void addStepToTransaction(
            String transactionId, String targetSystem, TransactionStatus status) {

        TransactionEntity transactionEntity = this.transactionRepository.findById(transactionId)
                .orElseThrow(); // TODO: handle exception
        transactionEntity.getSteps().add(new TransactionItem(targetSystem, status.toString()));
        this.transactionRepository.save(transactionEntity);
    }
}
