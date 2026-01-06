package com.ontop.balance.core.model.exceptions;

public class IllegalAmountValueException extends RuntimeException {

    private IllegalAmountValueException(String message) {
        super(message);
    }

    public static IllegalAmountValueException createIllegalAmountForNull() {
        return new IllegalAmountValueException("Amount cannot be null");
    }

    public static IllegalAmountValueException createIllegalAmountForNegativeValue() {
        return new IllegalAmountValueException("Amount cannot be negative");
    }
}
