package com.ontop.balance.core.ports.inbound;

import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.queries.ObtainTransactionByIdQuery;

public interface ObtainTransactionsById {

    TransactionData handler(ObtainTransactionByIdQuery query);

}
