package com.ontop.balance.core.model.exceptions;

public class ChargebackFailedException extends RuntimeException {

    public ChargebackFailedException(String message) {
        super(message);
    }
}
