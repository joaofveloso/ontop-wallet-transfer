package com.ontop.balance.core.model.exceptions;

public class TransactionFailedException extends RuntimeException {

    public TransactionFailedException(String message) {
        super(message);
    }
}
