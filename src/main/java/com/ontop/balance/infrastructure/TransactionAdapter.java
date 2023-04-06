package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.ports.outbound.Transaction;
import com.ontop.balance.infrastructure.entity.TransactionEntity;
import com.ontop.balance.infrastructure.repositories.TransactionRepository;
import java.util.Collections;
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

     //TODO: create sub transactions
       /*
        List<TransactionData.TransactionItemData> itemDataList = new ArrayList<>();
        for (TransactionItemEntity item : entity.getItems()) {
            itemDataList.add(new TransactionData.TransactionItemData(item.getCreatedAt(),
                    item.getTargetSystem(), item.getStatus()));
        }
        */

        return new TransactionData(entity.getId(), entity.getClientId(), entity.getCreatedAt(), Collections.emptyList());
    }

    @Override
    public void starNewTransaction(String transactionId, Long clientId, String recipientId, String name) {

        TransactionEntity transactionEntity = new TransactionEntity(transactionId, clientId, recipientId,
                name);
        transactionRepository.save(transactionEntity);
    }
}
