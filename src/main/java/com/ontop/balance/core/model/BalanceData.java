package com.ontop.balance.core.model;

import com.ontop.balance.core.model.exceptions.InsufficientBalanceException;
import java.math.BigDecimal;

public record BalanceData(BigDecimal ammount) {

    public void checkSufficientBalance(BigDecimal amount) {
        AmountValidation.moneyAmountValidation(amount);
        if (this.ammount().compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
    }
}