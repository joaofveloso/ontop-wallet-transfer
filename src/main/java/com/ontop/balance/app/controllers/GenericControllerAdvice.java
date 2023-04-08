package com.ontop.balance.app.controllers;

import com.ontop.balance.app.models.ErrorResponse;
import com.ontop.balance.app.models.ErrorResponse.SubErrorResponse;
import com.ontop.balance.core.model.exceptions.InsufficientBalanceException;
import com.ontop.balance.core.model.exceptions.RecipientNotFoundException;
import com.ontop.balance.core.model.exceptions.TransactionNotFoundException;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@ControllerAdvice
public class GenericControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
        log.warn("Validation failed {}", exception.getMessage());
        List<SubErrorResponse> subErrors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::getSubErrorResponse)
                .collect(Collectors.toList());
        return new ErrorResponse("Validation failed!", subErrors);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInsufficientBalanceException(InsufficientBalanceException exception) {
        log.warn("Insufficient balance: {}", exception.getMessage());
        return new ErrorResponse("Insufficient balance", Collections.singletonList(
                new ErrorResponse.SubErrorResponse("balance", "The account balance is not sufficient for this transaction")
        ));
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleTransactionNotFoundException(TransactionNotFoundException exception) {
        log.warn("Transaction not found: {}", exception.getMessage());
        return new ErrorResponse("Transaction not found", Collections.singletonList(
                new ErrorResponse.SubErrorResponse("transactionId", "The specified transaction ID could not be found")
        ));
    }

    @ExceptionHandler(RecipientNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleRecipientNotFoundException(RecipientNotFoundException exception) {
        log.warn("Recipient not found: {}", exception.getMessage());
        return new ErrorResponse("Recipient not found", Collections.singletonList(
                new ErrorResponse.SubErrorResponse("recipientId", "The specified Recipient Id could not be found")
        ));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestHeaderException(MissingRequestHeaderException exception) {
        log.warn("Missing request header: {}", exception.getMessage());
        return new ErrorResponse("Missing request header", Collections.singletonList(
                new ErrorResponse.SubErrorResponse("header", "The specified request header is missing: " + exception.getParameter().getParameterName())
        ));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleExpiredJwtException(ExpiredJwtException exception) {
        log.warn("Expired token {}", exception.getMessage());
        return new ErrorResponse("Expired token", Collections.singletonList(
                new ErrorResponse.SubErrorResponse("token", "The JWT token has expired")
        ));
    }

    private SubErrorResponse getSubErrorResponse(FieldError fieldError) {
        return new SubErrorResponse(fieldError.getField(), fieldError.getDefaultMessage());
    }
}