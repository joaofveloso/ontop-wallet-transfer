package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import java.util.Optional;

public interface Recipient {

    void save(CreateRecipientCommand createRecipientCommand, String uuid);
    PaginatedWrapper<RecipientData> findRecipients(Long clientId);
    Optional<RecipientData> findRecipientById(String id);
}
