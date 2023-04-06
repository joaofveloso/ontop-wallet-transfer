package com.ontop.balance.app.controllers;

import com.ontop.balance.app.LocationUtils;
import com.ontop.balance.app.models.TransactionItemResponse;
import com.ontop.balance.app.models.TransactionItemResponse.TransactionStepResponse;
import com.ontop.balance.app.models.TransferMoneyRequest;
import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.commands.TransferMoneyCommand;
import com.ontop.balance.core.model.queries.ObtainTransactionByIdQuery;
import com.ontop.balance.core.ports.inbound.ObtainTransactionsById;
import com.ontop.balance.core.ports.inbound.TransferMoney;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransferMoneyController implements TransferMoneyControllerDoc {

    private final TransferMoney transferMoney;
    private final ObtainTransactionsById obtainTransactionsById;

    @Override
    public ResponseEntity<Void> createTransfer(Long clientId, TransferMoneyRequest request) {
        String transaction = this.transferMoney.handler(
                new TransferMoneyCommand(request.recipientId(), clientId, request.amount()));

        HttpHeaders headers = LocationUtils.getHttpHeadersWithLocation(transaction);
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<TransactionItemResponse> obtainTransactionsById(
            Long clientId, String transaction) {
        TransactionData handler = this.obtainTransactionsById.handler(
                new ObtainTransactionByIdQuery(transaction, clientId));
        return ResponseEntity.ok(
                new TransactionItemResponse(handler.transactionId(), handler.createdAt(),
                        handler.steps().stream().map(
                                step -> new TransactionStepResponse(step.createdAt(),
                                        step.targetSystem(), step.status().toString())).toList()));
    }
}
