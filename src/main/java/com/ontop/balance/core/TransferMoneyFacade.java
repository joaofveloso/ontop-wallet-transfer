package com.ontop.balance.core;

import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.balance.core.model.commands.TransferMoneyCommand;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.exceptions.RecipientNotFoundException;
import com.ontop.balance.core.model.exceptions.WalletNotFoundException;
import com.ontop.balance.core.model.queries.ObtainRecipientByIdQuery;
import com.ontop.balance.core.ports.inbound.TransferMoney;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.balance.core.ports.outbound.Recipient;
import com.ontop.balance.core.ports.outbound.Transaction;
import com.ontop.balance.core.ports.outbound.Wallet;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransferMoneyFacade implements TransferMoney{

    private final Recipient recipient;
    private final Wallet wallet;
    private final Payment payment;
    private final Transaction transaction;

    @Override
    public String handler(TransferMoneyCommand command) {

        final String transactionId = UUID.randomUUID().toString();

        ObtainRecipientByIdQuery obtainRecipientByIdQuery = new ObtainRecipientByIdQuery(
                command.recipientId(), command.clientId());

        RecipientData recipientData = this.recipient
                .findRecipientById(obtainRecipientByIdQuery)
                .orElseThrow(RecipientNotFoundException::new);
        recipientData.validateOwnership(command.clientId());

        BigDecimal withdrawAmount = command.amount();
        BigDecimal transferAmount = recipientData.applyFee(withdrawAmount);

        this.wallet.getBalance(recipientData.clientId())
                .orElseThrow(WalletNotFoundException::new)
                .checkSufficientBalance(withdrawAmount);

        this.transaction.starNewTransaction(transactionId, command.clientId(), recipientData.id(), recipientData.name());

        this.wallet.prepareWithdraw(withdrawAmount, recipientData, transactionId, consumerUpdateStepTransaction(transactionId));
        this.payment.prepareTransfer(transferAmount, recipientData, transactionId, consumerUpdateStepTransaction(transactionId));

        return transactionId;
    }

    private Consumer<String> consumerUpdateStepTransaction(String transactionId) {

        return targetSystem -> transaction.addStepToTransaction(transactionId,targetSystem,
                TransactionStatus.PENDING);
    }
}