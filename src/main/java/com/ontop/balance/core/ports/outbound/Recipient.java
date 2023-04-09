package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.model.queries.ObtainRecipientByClientQuery;
import com.ontop.balance.core.model.queries.ObtainRecipientByIdQuery;
import java.util.Optional;

public interface Recipient {

    void save(CreateRecipientCommand createRecipientCommand, String uuid);

    PaginatedWrapper<RecipientData> findRecipients(ObtainRecipientByClientQuery query);

    Optional<RecipientData> findRecipientById(ObtainRecipientByIdQuery query);
}
