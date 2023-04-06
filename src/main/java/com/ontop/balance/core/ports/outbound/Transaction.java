package com.ontop.balance.core.ports.outbound;

import com.ontop.balance.core.model.TransactionData;
import java.util.Optional;

public interface Transaction {

    Optional<TransactionData> getTransactionsById(String id);
}
