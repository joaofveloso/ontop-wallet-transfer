package com.ontop.balance.core.ports.inbound;

import com.ontop.balance.core.model.commands.CreateRecipientCommand;

public interface CreateRecipient {

    String handler(CreateRecipientCommand createRecipientCommand);
}
