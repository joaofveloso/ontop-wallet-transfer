package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.TransactionFacade;
import com.ontop.balance.core.model.BalanceData;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.kernels.WalletMessage;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface Wallet {

    TransactionStatus prepareWithdraw(BigDecimal amount, RecipientData recipientData, String transactionId);
    TransactionStatus withdraw(WalletMessage message);
    Optional<BalanceData> getBalance(Long clientId);
}
