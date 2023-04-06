package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.BalanceData;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.ports.outbound.Wallet;
import com.ontop.balance.infrastructure.clients.WalletClient;
import com.ontop.balance.infrastructure.clients.WalletClient.BalanceResponse;
import com.ontop.balance.infrastructure.messages.TransferMessage;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletAdapter implements Wallet {

    @Value("${core.topic}")
    private String topic;

    private final WalletClient walletClient;
    private final KafkaTemplate<String, TransferMessage> template;

    @Override
    public void chargeback(String transactionId) {

    }

    @Override
    public void withdraw(BigDecimal amount, RecipientData recipientData, String transactionId) {
        ProducerRecord<String, TransferMessage> record = new ProducerRecord<>(this.topic,
                new TransferMessage(recipientData.clientId(), recipientData.id(),
                        recipientData.name(), recipientData.routingNumber(),
                        recipientData.nationalIdentification(), recipientData.accountNumber(),
                        amount, transactionId));
        record.headers().add("x-transaction-id", transactionId.getBytes(StandardCharsets.UTF_8));
        this.template.send(record);
    }

    @Override
    public Optional<BalanceData> getBalance(Long clientId) {
        BalanceResponse balance = walletClient.getBalance(clientId);
        return Optional.of(new BalanceData(BigDecimal.valueOf(balance.balance())));
    }
}
