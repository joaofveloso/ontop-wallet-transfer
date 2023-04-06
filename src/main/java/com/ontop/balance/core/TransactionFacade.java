package com.ontop.balance.core;

import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.queries.ObtainTransactionByIdQuery;
import com.ontop.balance.core.ports.inbound.ObtainTransactionsById;
import com.ontop.balance.core.ports.outbound.Transaction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionFacade implements ObtainTransactionsById {

    private final Transaction transaction;

    @Override
    public TransactionData handler(ObtainTransactionByIdQuery query) {
        TransactionData transactionsById = transaction.getTransactionsById(query.id()).orElseThrow(); //TODO: throw exception
        transactionsById.validateOwnership(query.clientId());
        return transactionsById;
    }
}
