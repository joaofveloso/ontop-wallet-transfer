package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.BalanceData;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.kernels.WalletMessage;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Wallet {

    void chargeback(String transactionId);
    void prepareWithdraw(BigDecimal amount, RecipientData recipientData, String transactionId,
            Consumer<String> stringConsumer);
    void withdraw(WalletMessage message, BiConsumer<String, TransactionStatus> consumer);
    Optional<BalanceData> getBalance(Long clientId);
}
