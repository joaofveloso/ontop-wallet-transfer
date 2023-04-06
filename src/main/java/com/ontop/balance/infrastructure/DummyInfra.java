package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.BalanceData;
import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.PaginatedWrapper.PaginatedData;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.TransactionData;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.balance.core.ports.outbound.Recipient;
import com.ontop.balance.core.ports.outbound.Transaction;
import com.ontop.balance.core.ports.outbound.Wallet;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DummyInfra implements Payment, Transaction {

    @Override
    public void transfer(BigDecimal amount, RecipientData recipientData, String transactionId) {

    }

    @Override
    public Optional<TransactionData> getTransactionsById(String id) {
        return Optional.empty();
    }
}
