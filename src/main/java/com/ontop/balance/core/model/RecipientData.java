package com.ontop.balance.core.model;

import com.ontop.balance.core.model.exceptions.InvalidFeeException;
import com.ontop.balance.core.model.exceptions.UnauthorizedAccessToResourceException;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.web.client.HttpClientErrorException.Unauthorized;

public record RecipientData(
        String id, Long clientId, String name, String routingNumber, String nationalIdentification,
        String accountNumber, BigDecimal fee) {

    public RecipientData {
        if (fee.compareTo(BigDecimal.ZERO) < 0 || fee.compareTo(BigDecimal.ONE) > 0) {
            throw new InvalidFeeException();
        }
    }

    public void validateOwnership(Long clientId) {
        if (!Objects.equals(this.clientId, clientId)) {
            throw new UnauthorizedAccessToResourceException();
        }
    }

    public BigDecimal applyFee(BigDecimal amount) {
        AmountValidation.moneyAmountValidation(amount);
        return amount.multiply(BigDecimal.ONE.subtract(fee));
    }
}
