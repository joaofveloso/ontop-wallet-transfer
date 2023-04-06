package com.ontop.balance.core.ports.inbound;

import com.ontop.balance.app.models.RecipientResponse.RecipientResponseItem;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.queries.ObtainRecipientByIdQuery;

public interface ObtainRecipientById {

    RecipientData handler(ObtainRecipientByIdQuery query);
}
