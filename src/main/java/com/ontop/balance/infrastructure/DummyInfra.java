package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.BalanceData;
import com.ontop.balance.core.model.PaginatedWrapper;
import com.ontop.balance.core.model.PaginatedWrapper.PaginatedData;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.commands.CreateRecipientCommand;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.balance.core.ports.outbound.Recipient;
import com.ontop.balance.core.ports.outbound.Wallet;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DummyInfra implements Payment, Recipient, Wallet {

    @Override
    public void transfer(BigDecimal amount, RecipientData recipientData, String transactionId) {

    }

    @Override
    public void save(CreateRecipientCommand createRecipientCommand, String uuid) {

    }

    @Override
    public PaginatedWrapper<RecipientData> findRecipients(Long clientId) {

        return new PaginatedWrapper<>(
                List.of(new RecipientData("$UUID", 5L, "John Doe", "123456", "4564", "4056", BigDecimal.valueOf(0.07))),
                new PaginatedData(0,1,5));
    }

    @Override
    public Optional<RecipientData> findRecipientById(String id) {
        return Optional.empty();
    }


    @Override
    public void chargeback(String transactionId) {

    }

    @Override
    public void withdraw(BigDecimal amount, RecipientData recipientData, String transactionId) {

    }

    @Override
    public Optional<BalanceData> getBalance(Long clientId) {
        return Optional.empty();
    }
}
