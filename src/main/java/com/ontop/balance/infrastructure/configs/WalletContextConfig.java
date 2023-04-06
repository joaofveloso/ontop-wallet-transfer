package com.ontop.balance.infrastructure.configs;

import com.ontop.balance.core.RecipientFacadeByClient;
import com.ontop.balance.core.TransactionFacade;
import com.ontop.balance.core.TransferMoneyFacade;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.balance.core.ports.outbound.Recipient;
import com.ontop.balance.core.ports.outbound.Transaction;
import com.ontop.balance.core.ports.outbound.Wallet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalletContextConfig {

    @Bean
    RecipientFacadeByClient recipientFacade(Recipient recipient) {
        return new RecipientFacadeByClient(recipient);
    }

    @Bean
    TransferMoneyFacade transferMoneyFacade(Payment payment, Recipient recipient, Wallet wallet) {
        return new TransferMoneyFacade(recipient, wallet, payment);
    }

    @Bean
    TransactionFacade transactionFacade(Transaction transaction) {
        return new TransactionFacade(transaction);
    }
}
