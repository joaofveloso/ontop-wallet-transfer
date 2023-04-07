package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.kernels.PaymentMessage;
import com.ontop.kernels.WalletMessage;
import java.math.BigDecimal;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Payment {

    void prepareTransfer(BigDecimal amount, RecipientData recipientData, String transactionId,
            Consumer<String> stringConsumer);
    void transfer(PaymentMessage message, BiConsumer<String, TransactionStatus> consumer);
}
