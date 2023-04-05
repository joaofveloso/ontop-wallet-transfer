package com.ontop.balance.core.model;

import com.ontop.balance.core.model.exceptions.IllegalAmountValueExcpetion;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AmountValidation {

    public static void moneyAmountValidation(BigDecimal amount) {
        if (amount == null) {
            throw IllegalAmountValueExcpetion.createIllegalAmountForNull();
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw IllegalAmountValueExcpetion.createIllegallAmountForNegativeValue();
        }
    }
}
