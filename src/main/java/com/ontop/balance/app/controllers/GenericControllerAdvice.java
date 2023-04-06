package com.ontop.balance.app.controllers;

import com.ontop.balance.app.models.ErrorResponse;
import com.ontop.balance.app.models.ErrorResponse.SubErrorResponse;
import com.ontop.balance.core.model.exceptions.InsufficientBalanceException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ControllerAdvice
public class GenericControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception) {
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
        return new ErrorResponse("Insufficient balance", Collections.singletonList(
                new ErrorResponse.SubErrorResponse("balance", "The account balance is not sufficient for this transaction")
        ));
    }

    private SubErrorResponse getSubErrorResponse(FieldError fieldError) {
        return new SubErrorResponse(fieldError.getField(), fieldError.getDefaultMessage());
    }
}