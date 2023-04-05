package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.RecipientData;
import java.math.BigDecimal;

public interface Payment {

    void transfer(BigDecimal amount, RecipientData recipientData, String transactionId);
}
