package com.ontop.balance.infrastructure.configs;

import com.ontop.balance.core.RecipientFacade;
import com.ontop.balance.core.TransactionFacade;
import com.ontop.balance.core.TransferMoneyFacade;
import com.ontop.balance.core.ports.outbound.Chargeback;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.balance.core.ports.outbound.Recipient;
import com.ontop.balance.core.ports.outbound.Transaction;
import com.ontop.balance.core.ports.outbound.Wallet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WalletContextConfig {

    @Bean
    RecipientFacade recipientFacade(Recipient recipient) {
        return new RecipientFacade(recipient);
    }

    @Bean
    TransferMoneyFacade transferMoneyFacade(Payment payment, Recipient recipient, Wallet wallet,
            Transaction transaction, Chargeback chargeback) {
        return new TransferMoneyFacade(recipient, wallet, payment, transaction, chargeback);
    }

    @Bean
    TransactionFacade transactionFacade(Transaction transaction, Payment payment, Wallet wallet,
            Chargeback chargeback) {
        return new TransactionFacade(transaction, payment, wallet, chargeback);
    }
}
