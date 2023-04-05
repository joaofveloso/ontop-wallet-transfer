package com.ontop.balance.core.ports.inbound;

import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.queries.ObtainRecipientQuery;
import java.util.List;

public interface ObtainRecipient {

    List<RecipientData> handler(ObtainRecipientQuery query);
}
