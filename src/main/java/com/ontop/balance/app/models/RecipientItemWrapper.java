package com.ontop.balance.app.models;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ontop.balance.app.controllers.RecipientController;
import com.ontop.balance.app.models.RecipientResponse.RecipientResponseItem;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;

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
