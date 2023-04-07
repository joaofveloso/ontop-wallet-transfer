package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.BalanceData;
import com.ontop.balance.core.model.RecipientData;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;

public interface Wallet {

    void chargeback(String transactionId);
    void withdraw(BigDecimal amount, RecipientData recipientData, String transactionId,
            Consumer<String> stringConsumer);
    Optional<BalanceData> getBalance(Long clientId);
}
