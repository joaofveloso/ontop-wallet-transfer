package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.model.RecipientData;
import java.util.List;
import java.util.Optional;

public interface Recipient {

    void save(CreateRecipientCommand createRecipientCommand);
    List<RecipientData> findRecipients(Long clientId);
    Optional<RecipientData> findRecipientById(Long recipientId);
}
