package com.ontop.balance.core.ports.inbound;

import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.queries.ObtainRecipientByClientQuery;

public interface ObtainRecipientByClient {

    PaginatedWrapper<RecipientData> handler(ObtainRecipientByClientQuery query);
}
