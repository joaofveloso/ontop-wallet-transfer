package com.ontop.balance.core;

import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.queries.ObtainRecipientQuery;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.ports.inbound.CreateRecipient;
import com.ontop.balance.core.ports.inbound.ObtainRecipient;
import com.ontop.balance.core.ports.outbound.Recipient;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecipientFacade implements CreateRecipient, ObtainRecipient {

    private final Recipient recipient;

    @Override
    public void handler(CreateRecipientCommand command) {
        this.recipient.save(command);
    }

    @Override
    public List<RecipientData> handler(ObtainRecipientQuery query) {
        return this.recipient.findRecipients(query.clientId());
    }
}
