package com.ontop.balance.app.models;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ontop.balance.app.controllers.RecipientController;
import com.ontop.balance.app.models.RecipientResponse.RecipientResponseItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

@SuppressWarnings("FieldCanBeLocal")
@Getter
@Schema(description = "Wrapper object for a recipient response item")
public class RecipientItemWrapper extends RepresentationModel<RecipientItemWrapper> {

    private final RecipientResponseItem data;

    public RecipientItemWrapper(RecipientResponseItem data, Link... links) {
        this.data = data;
        add(links);
        add(linkTo(methodOn(RecipientController.class)
                .obtainRecipientById(null, data.id()))
                .withSelfRel());
    }
}
