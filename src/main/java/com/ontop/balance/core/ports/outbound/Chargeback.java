package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.kernels.ChargebackMessage;

public interface Chargeback {

    TransactionStatus prepareChargeback(String transactionId);
    TransactionStatus chargeback(ChargebackMessage message);
}
