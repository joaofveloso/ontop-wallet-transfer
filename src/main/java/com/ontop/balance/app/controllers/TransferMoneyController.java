package com.ontop.balance.app.controllers;

import com.ontop.balance.app.LocationUtils;
import com.ontop.balance.app.models.MetadataResponse;
import com.ontop.balance.app.models.PaginationResponse;
import com.ontop.balance.app.models.TransactionItemResponse;
import com.ontop.balance.app.models.TransactionItemResponse.TransactionStepResponse;
import com.ontop.balance.app.models.TransactionResponse;
import com.ontop.balance.app.models.TransactionResponseWrapper;
import com.ontop.balance.app.models.TransferMoneyRequest;
import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.PaginatedWrapper.PaginatedData;
import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.TransactionData.TransactionItemData;
import com.ontop.balance.core.model.commands.TransferMoneyCommand;
import com.ontop.balance.core.model.queries.ObtainTransactionByIdQuery;
import com.ontop.balance.core.model.queries.ObtainTransactionClientQuery;
import com.ontop.balance.core.ports.inbound.ObtainTransactionByClient;
import com.ontop.balance.core.ports.inbound.ObtainTransactionsById;
import com.ontop.balance.core.ports.inbound.TransferMoney;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
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
    private final ObtainTransactionByClient obtainTransactionByClient;

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
        return ResponseEntity.ok(toTransactionItemResponse(handler, true));
    }

    @Override
    public ResponseEntity<TransactionResponseWrapper> obtainTransactionsByClient(Long clientId,
            LocalDate dateToFilter, int page, int size) {
        PaginatedWrapper<TransactionData> handler = this.obtainTransactionByClient.handler(
                new ObtainTransactionClientQuery(clientId, dateToFilter, page, size));
        TransactionResponse wrapper = toTransactionResponseWrapper(handler, new MetadataResponse(
                Map.of("FilterDate", String.valueOf(dateToFilter))), true);
        return ResponseEntity.ok(new TransactionResponseWrapper(wrapper));
    }

    private TransactionResponse toTransactionResponseWrapper(PaginatedWrapper<TransactionData> wrapper, MetadataResponse meta, boolean onlyLastStep) {

        List<TransactionItemResponse> transactions = wrapper.data().stream()
                .map(m -> toTransactionItemResponse(m, onlyLastStep)).toList();

        PaginatedData pagination = wrapper.pagination();
        PaginationResponse paginationResponse = new PaginationResponse(pagination.page(),
                pagination.totalPages(), pagination.totalPages());

        return new TransactionResponse(transactions, paginationResponse, meta);
    }

    private TransactionItemResponse toTransactionItemResponse(TransactionData data, boolean onlyLastStep) {

        return new TransactionItemResponse(data.transactionId(), data.createdAt(),
                getSteps(data.steps(), onlyLastStep).stream().map(this::toTransactionStepResponse).toList());
    }

    private List<TransactionItemData> getSteps(List<TransactionItemData> step, boolean onlyLastStep) {
        return onlyLastStep ? step.stream()
                .collect(Collectors.groupingBy(TransactionItemData::targetSystem, Collectors.maxBy(
                        Comparator.comparing(TransactionItemData::createdAt)))).values().stream()
                .map(Optional::get).toList() : step;
    }

    private TransactionStepResponse toTransactionStepResponse(TransactionItemData data) {
        return new TransactionStepResponse(data.createdAt(), data.targetSystem(), data.status().toString());
    }
}
