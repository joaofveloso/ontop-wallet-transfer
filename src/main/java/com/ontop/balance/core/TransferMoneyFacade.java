package com.ontop.balance.core;

import com.ontop.balance.core.model.commands.TransferMoneyCommand;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.exceptions.RecipientNotFoundException;
import com.ontop.balance.core.model.exceptions.WalletNotFoundException;
import com.ontop.balance.core.ports.inbound.TransferMoney;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.balance.core.ports.outbound.Recipient;
import com.ontop.balance.core.ports.outbound.Wallet;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransferMoneyFacade implements TransferMoney {

    private final Recipient recipient;
    private final Wallet wallet;
    private final Payment payment;

    @Override
    public void handler(TransferMoneyCommand transferCommand) {

        final String transactionId = UUID.randomUUID().toString();

        RecipientData recipientData = this.recipient.findRecipientById(
                transferCommand.recipientId()).orElseThrow(RecipientNotFoundException::new);

        BigDecimal withdrawAmount = transferCommand.amount();
        BigDecimal transferAmount = recipientData.applyFee(withdrawAmount);

        this.wallet.getBalance(recipientData.clientId()).orElseThrow(WalletNotFoundException::new)
                .checkSufficientBalance(withdrawAmount);

        this.wallet.withdraw(withdrawAmount, recipientData, transactionId);
        this.payment.transfer(transferAmount, recipientData, transactionId);
    }
}