package com.ontop.balance.core;

import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.model.exceptions.RecipientNotFoundException;
import com.ontop.balance.core.model.queries.ObtainRecipientByClientQuery;
import com.ontop.balance.core.model.queries.ObtainRecipientByIdQuery;
import com.ontop.balance.core.ports.inbound.CreateRecipient;
import com.ontop.balance.core.ports.inbound.ObtainRecipientByClient;
import com.ontop.balance.core.ports.inbound.ObtainRecipientById;
import com.ontop.balance.core.ports.outbound.Recipient;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecipientFacade implements CreateRecipient, ObtainRecipientByClient,
        ObtainRecipientById {

    private final Recipient recipient;

    @Override
    public String handler(CreateRecipientCommand command) {
        String uuid = UUID.randomUUID().toString();
        this.recipient.save(command, uuid);
        return uuid;
    }

    @Override
    public PaginatedWrapper<RecipientData> handler(ObtainRecipientByClientQuery query) {
        return this.recipient.findRecipients(query);
    }

    @Override
    public RecipientData handler(ObtainRecipientByIdQuery query) {
        RecipientData recipientData = this.recipient.findRecipientById(query)
                .orElseThrow(RecipientNotFoundException::new);
        recipientData.validateOwnership(query.clientId());
        return recipientData;
    }
}
