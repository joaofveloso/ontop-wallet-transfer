package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.BalanceData;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.ports.outbound.Wallet;
import com.ontop.balance.infrastructure.clients.WalletClient;
import com.ontop.balance.infrastructure.clients.WalletClient.BalanceResponse;
import com.ontop.kernels.WalletMessage;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;

@Component
@RequiredArgsConstructor
public class WalletAdapter implements Wallet {

    @Value("${core.topic}")
    private String topic;

    private final WalletClient walletClient;
    private final KafkaTemplate<String, WalletMessage> walletProducer;

    @Override
    public void chargeback(String transactionId) {

    }

    @Override
    public void withdraw(BigDecimal amount, RecipientData recipientData, String transactionId,
            Consumer<String> stringConsumer) {
        ProducerRecord<String, WalletMessage> walletRecord = new ProducerRecord<>(this.topic,
                transactionId, new WalletMessage(recipientData.clientId(), amount, transactionId));
        walletRecord.headers().add("x-transaction-id", transactionId.getBytes(StandardCharsets.UTF_8));
        try {
            var send = this.walletProducer.send(walletRecord);
            send.addCallback(success -> {
                stringConsumer.accept(this.getClass().getSimpleName());
            }, ex -> {});
            send.get();

            //TODO: Deal with other exceptions
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<BalanceData> getBalance(Long clientId) {
        BalanceResponse balance = walletClient.getBalance(clientId);
        return Optional.of(new BalanceData(BigDecimal.valueOf(balance.balance())));
    }
}
