package com.ontop.balance.core.ports.inbound;

import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.queries.ObtainTransactionClientQuery;

public interface ObtainTransactionByClient {

    PaginatedWrapper<TransactionData> handler(ObtainTransactionClientQuery query);
}
