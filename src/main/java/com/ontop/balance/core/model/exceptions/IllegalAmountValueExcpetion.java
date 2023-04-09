package com.ontop.balance.core.model.exceptions;

public class IllegalAmountValueExcpetion extends RuntimeException {

    private IllegalAmountValueExcpetion(String message) {
        super(message);
    }

    public static IllegalAmountValueExcpetion createIllegalAmountForNull() {
        return new IllegalAmountValueExcpetion("Amount cannot be null");
    }

    public static IllegalAmountValueExcpetion createIllegallAmountForNegativeValue() {
        return new IllegalAmountValueExcpetion("Amount cannot be negative");
    }
}
