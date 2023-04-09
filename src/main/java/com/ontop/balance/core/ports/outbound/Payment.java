package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.kernels.PaymentMessage;
import java.math.BigDecimal;

public interface Payment {

    TransactionStatus prepareTransfer(BigDecimal amount, RecipientData recipientData,
            String transactionId);

    TransactionStatus transfer(PaymentMessage message);
}
