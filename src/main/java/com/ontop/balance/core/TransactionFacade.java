package com.ontop.balance.core;

import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.balance.core.model.exceptions.ChargebackFailedException;
import com.ontop.balance.core.model.exceptions.TransactionNotFoundException;
import com.ontop.balance.core.model.queries.ObtainTransactionByIdQuery;
import com.ontop.balance.core.model.queries.ObtainTransactionClientQuery;
import com.ontop.balance.core.ports.inbound.ExecuteChargebackTransaction;
import com.ontop.balance.core.ports.inbound.ExecutePaymentTransaction;
import com.ontop.balance.core.ports.inbound.ExecuteWalletTransaction;
import com.ontop.balance.core.ports.inbound.ObtainTransactionByClient;
import com.ontop.balance.core.ports.inbound.ObtainTransactionsById;
import com.ontop.balance.core.ports.outbound.Chargeback;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.balance.core.ports.outbound.Transaction;
import com.ontop.balance.core.ports.outbound.Wallet;
import com.ontop.balance.infrastructure.ChargebackAdapter;
import com.ontop.balance.infrastructure.PaymentAdapter;
import com.ontop.balance.infrastructure.WalletAdapter;
import com.ontop.kernels.ChargebackMessage;
import com.ontop.kernels.PaymentMessage;
import com.ontop.kernels.WalletMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TransactionFacade implements ObtainTransactionsById, ObtainTransactionByClient,
        ExecuteWalletTransaction, ExecutePaymentTransaction, ExecuteChargebackTransaction {

    private final Transaction transaction;
    private final Payment payment;
    private final Wallet wallet;
    private final Chargeback chargeback;

    @Override
    public TransactionData handler(ObtainTransactionByIdQuery query) {
        TransactionData transactionsById = transaction.getTransactionsById(query.id())
                .orElseThrow(TransactionNotFoundException::new);
        transactionsById.validateOwnership(query.clientId());
        return transactionsById;
    }

    @Override
    public void handle(WalletMessage message) {
        this.transaction.addStepToTransaction(message.getTransactionId(),
                WalletAdapter.class.getSimpleName(), TransactionStatus.IN_PROGRESS);
        TransactionStatus status = this.wallet.withdraw(message);
        this.transaction.addStepToTransaction(message.getTransactionId(),
                WalletAdapter.class.getSimpleName(), status);
    }

    @Override
    public void handle(PaymentMessage message) {
        this.transaction.addStepToTransaction(message.getTransactionId(),
                PaymentAdapter.class.getSimpleName(), TransactionStatus.IN_PROGRESS);
        TransactionStatus status = this.payment.transfer(message);
        this.transaction.addStepToTransaction(message.getTransactionId(),
                PaymentAdapter.class.getSimpleName(), status);
        if (TransactionStatus.FAILED.equals(status)) {
            TransactionStatus chargeBackStatus = this.chargeback.prepareChargeback(
                    message.getTransactionId());
            this.transaction.addStepToTransaction(message.getTransactionId(),
                    ChargebackAdapter.class.getSimpleName(), chargeBackStatus);
        }
    }

    @Override
    public void handle(ChargebackMessage message) {
        try {
            this.transaction.addStepToTransaction(message.getTransactionId(),
                    ChargebackAdapter.class.getSimpleName(), TransactionStatus.IN_PROGRESS);
            TransactionStatus status = this.chargeback.chargeback(message);
            this.transaction.addStepToTransaction(message.getTransactionId(),
                    ChargebackAdapter.class.getSimpleName(), status);
            if (!TransactionStatus.COMPLETED.equals(status)) {
                throw new ChargebackFailedException(
                        "Chargeback failed for transaction ID " + message.getTransactionId());
            }
        } catch (Exception e) {
            log.error("Critical failure: {}", e.getMessage(), e);
        }
    }

    @Override
    public PaginatedWrapper<TransactionData> handler(ObtainTransactionClientQuery query) {
        return this.transaction.findByClient(query);
    }
}
