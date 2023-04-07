package com.ontop.balance.core;

import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.balance.core.model.commands.TransferMoneyCommand;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.exceptions.OwnershipValidationException;
import com.ontop.balance.core.model.exceptions.RecipientNotFoundException;
import com.ontop.balance.core.model.exceptions.TransactionFailedException;
import com.ontop.balance.core.model.exceptions.WalletNotFoundException;
import com.ontop.balance.core.model.queries.ObtainRecipientByIdQuery;
import com.ontop.balance.core.ports.inbound.TransferMoney;
import com.ontop.balance.core.ports.outbound.*;
import com.ontop.balance.infrastructure.PaymentAdapter;
import com.ontop.balance.infrastructure.WalletAdapter;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class TransferMoneyFacade implements TransferMoney{

    private final Recipient recipient;
    private final Wallet wallet;
    private final Payment payment;
    private final Transaction transaction;
    private final Chargeback chargeback;

    @Override
    public String handler(TransferMoneyCommand command) {
        String transactionId = UUID.randomUUID().toString();

        RecipientData recipientData = getRecipientData(command);

        checkRecipientOwnership(command.clientId(), recipientData);

        BigDecimal withdrawAmount = command.amount();
        BigDecimal transferAmount = recipientData.applyFee(withdrawAmount);

        checkSufficientBalance(recipientData.clientId(), withdrawAmount);

        startTransaction(transactionId, command, recipientData);

        prepareWithdraw(withdrawAmount, recipientData, transactionId);

        TransactionStatus transactionPaymentStatus =
                prepareTransfer(transferAmount, recipientData, transactionId);

        if (transactionPaymentStatus.equals(TransactionStatus.FAILED)) {
            prepareChargeback(transactionId);
        }

        return transactionId;
    }

    private RecipientData getRecipientData(TransferMoneyCommand command) {
        ObtainRecipientByIdQuery obtainRecipientByIdQuery = new ObtainRecipientByIdQuery(
                command.recipientId(), command.clientId());

        return recipient.findRecipientById(obtainRecipientByIdQuery)
                .orElseThrow(RecipientNotFoundException::new);
    }

    private void checkRecipientOwnership(Long clientId, RecipientData recipientData) {
        if (!recipientData.isOwnedBy(clientId)) {
            throw new OwnershipValidationException("Recipient is not owned by client");
        }
    }

    private void checkSufficientBalance(Long clientId, BigDecimal withdrawAmount) {
        wallet.getBalance(clientId)
                .orElseThrow(WalletNotFoundException::new)
                .checkSufficientBalance(withdrawAmount);
    }

    private void startTransaction(String transactionId, TransferMoneyCommand command, RecipientData recipientData) {
        this.transaction.starNewTransaction(transactionId, command, recipientData);
    }

    private void prepareWithdraw(BigDecimal withdrawAmount, RecipientData recipientData, String transactionId) {
        TransactionStatus transactionWalletStatus = wallet.prepareWithdraw(withdrawAmount, recipientData, transactionId);
        transaction.addStepToTransaction(transactionId, WalletAdapter.class.getSimpleName(), transactionWalletStatus);
        if (transactionWalletStatus.equals(TransactionStatus.FAILED)) {
            throw new TransactionFailedException("Transaction failed at wallet step");
        }
    }

    private TransactionStatus prepareTransfer(BigDecimal transferAmount, RecipientData recipientData, String transactionId) {
        TransactionStatus transactionPaymentStatus = payment.prepareTransfer(transferAmount, recipientData, transactionId);
        transaction.addStepToTransaction(transactionId, PaymentAdapter.class.getSimpleName(), transactionPaymentStatus);
        return transactionPaymentStatus;
    }

    private void prepareChargeback(String transactionId) {
        this.chargeback.prepareChargeback(transactionId);
        throw new TransactionFailedException("Transaction failed at payment step, chargeback initiated");
    }
}