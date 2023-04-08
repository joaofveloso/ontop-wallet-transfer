package com.ontop.balance.app.models;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.ontop.balance.app.controllers.TransferMoneyControllerDoc;
import java.time.LocalDate;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

@Getter
public class TransactionResponseWrapper extends RepresentationModel<TransactionResponseWrapper> {

    private final @JsonUnwrapped TransactionResponse response;

    public TransactionResponseWrapper(TransactionResponse response, Link... links) {
        this.response = response;
        PaginationResponse pagination = response.pagination();
        MetadataResponse meta = response.meta();
        LocalDate filterDate = parseDate(meta.properties().get("FilterDate"));

        add(links);
        add(linkTo(methodOn(TransferMoneyControllerDoc.class).obtainTransactionsByClient(null,
                filterDate, pagination.page(), pagination.pageSize())).withSelfRel());
        if (pagination.page() > 0) {
            add(linkTo(methodOn(TransferMoneyControllerDoc.class).obtainTransactionsByClient(null,
                    filterDate, pagination.page() - 1, pagination.pageSize())).withRel("previous"));
        }
        if (pagination.page() < pagination.totalPages() - 1) {
            add(linkTo(methodOn(TransferMoneyControllerDoc.class).obtainTransactionsByClient(null,
                    filterDate, pagination.page() + 1, pagination.pageSize())).withRel("next"));
        }
    }

    private LocalDate parseDate(String date) {
        return "null".equals(date) ? null : LocalDate.parse(date);
    }
}
