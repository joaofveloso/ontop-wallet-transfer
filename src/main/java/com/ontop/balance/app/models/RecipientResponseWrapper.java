package com.ontop.balance.app.models;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.ontop.balance.app.controllers.RecipientController;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Schema(description = "Wrapper object for a recipient response")
public class RecipientResponseWrapper extends RepresentationModel<RecipientResponseWrapper> {

    private final @JsonUnwrapped RecipientResponse response;

    public RecipientResponseWrapper(RecipientResponse response, Link... links) {
        this.response = response;
        PaginationResponse pagination = response.pagination();
        add(links);
        add(linkTo(methodOn(RecipientController.class).obtainRecipients(null, pagination.page(),
                pagination.pageSize())).withSelfRel());
        if (pagination.page() > 0) {
            add(linkTo(methodOn(RecipientController.class).obtainRecipients(null,
                    pagination.page() - 1, pagination.pageSize())).withRel("previous"));
        }
        if (pagination.page() < pagination.totalPages() - 1) {
            add(linkTo(methodOn(RecipientController.class).obtainRecipients(null,
                    pagination.page() + 1, pagination.pageSize())).withRel("next"));
        }
    }
}
