package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.ports.outbound.Payment;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class DummyInfra implements Payment {

    @Override
    public void transfer(BigDecimal amount, RecipientData recipientData, String transactionId) {

    }
}
