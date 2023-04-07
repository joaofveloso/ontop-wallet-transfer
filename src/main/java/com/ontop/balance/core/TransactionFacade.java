package com.ontop.balance.core;

import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.queries.ObtainTransactionByIdQuery;
import com.ontop.balance.core.ports.inbound.ExecuteChargebackTransaction;
import com.ontop.balance.core.ports.inbound.ExecutePaymentTransaction;
import com.ontop.balance.core.ports.inbound.ExecuteWalletTransaction;
import com.ontop.balance.core.ports.inbound.ObtainTransactionsById;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.balance.core.ports.outbound.Transaction;
import com.ontop.balance.core.ports.outbound.Wallet;
import com.ontop.kernels.PaymentMessage;
import com.ontop.kernels.WalletChargebackMessage;
import com.ontop.kernels.WalletMessage;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionFacade implements ObtainTransactionsById, ExecuteWalletTransaction,
        ExecutePaymentTransaction, ExecuteChargebackTransaction {

    private final Transaction transaction;
    private final Payment payment;
    private final Wallet wallet;

    @Override
    public TransactionData handler(ObtainTransactionByIdQuery query) {
        TransactionData transactionsById = transaction.getTransactionsById(query.id()).orElseThrow(); //TODO: throw exception
        transactionsById.validateOwnership(query.clientId());
        return transactionsById;
    }

    @Override
    public void handle(WalletMessage message) {
        this.wallet.withdraw(message, (targetSystem, status) -> transaction.addStepToTransaction(
                message.getTransactionId(), targetSystem, status));
    }

    @Override
    public void handle(PaymentMessage message) {
        this.payment.transfer(message, (targetSystem, status) -> transaction.addStepToTransaction(
                message.getTransactionId(), targetSystem, status));
    }

    @Override
    public void handle(WalletChargebackMessage message) {

    }
}
