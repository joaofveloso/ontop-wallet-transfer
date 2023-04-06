package com.ontop.balance.core.model;

import com.ontop.balance.core.model.exceptions.InvalidFeeException;
import java.math.BigDecimal;

public record RecipientData(
        String id, Long clientId, String name, String routingNumber, String nationalIdentification,
        String accountNumber, BigDecimal fee) {

    public RecipientData {
        if (fee.compareTo(BigDecimal.ZERO) < 0 || fee.compareTo(BigDecimal.ONE) > 0) {
            throw new InvalidFeeException();
        }
    }

    public BigDecimal applyFee(BigDecimal amount) {
        AmountValidation.moneyAmountValidation(amount);
        return amount.multiply(BigDecimal.ONE.subtract(fee));
    }
}
