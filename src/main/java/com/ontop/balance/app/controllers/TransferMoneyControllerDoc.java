package com.ontop.balance.app.controllers;

import com.ontop.balance.app.models.TransactionItemResponse;
import com.ontop.balance.app.models.TransactionResponseWrapper;
import com.ontop.balance.app.models.TransferMoneyRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/transactions")
@SecurityScheme(name = HttpHeaders.AUTHORIZATION, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@Tag(name = "Transfer Money", description = "Endpoints for creating money transfer requests")
public interface TransferMoneyControllerDoc {

    @PostMapping
    @SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
    @Operation(summary = "Create a new money transfer request", description = """
            Creates a new money transfer request with the specified recipient ID and transfer\040
            amount. The request body should include a JSON object with the recipient ID and\040
            transfer amount, which will be used to create the new money transfer request. The\040
            server will generate a unique account ID for the new account, which will be\040
            returned in the response headers under the `Location` key. If the operation is\040
            successful, the response status code will be `200 OK`.""")
    ResponseEntity<Void> createTransfer(
            @Parameter(hidden = true) @RequestHeader("X-Client-Id") Long clientId,
            @RequestBody @Valid TransferMoneyRequest request);

    @GetMapping("/{id}")
    @SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
    @Operation(summary = "Retrieve transactions by ID", description = """
            Retrieves a transaction by the specified ID. If the transaction exists,\040
            the response status code will be `200 OK`. If the transaction does not exist,\040
            the response status code will be `404 Not Found`.""")
    ResponseEntity<TransactionItemResponse> obtainTransactionsById(
            @Parameter(hidden = true) @RequestHeader("X-Client-Id") Long clientId,
            @PathVariable("id") String transaction);

    @GetMapping
    @SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
    @Operation(summary = "Obtain transactions by client", description = """
            Retrieves a list of transactions associated with the authenticated client. Optionally,\040
            you can filter by date using the `dateToFilter` parameter. The response includes\040
            pagination information as well as metadata about the request.""")
    ResponseEntity<TransactionResponseWrapper> obtainTransactionsByClient(
            @Parameter(hidden = true) @RequestHeader("X-Client-Id") Long clientId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dateToFilter,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size);
}
