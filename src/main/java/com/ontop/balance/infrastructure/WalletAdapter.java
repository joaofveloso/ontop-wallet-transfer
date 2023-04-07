package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.BalanceData;
import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.balance.core.ports.outbound.Wallet;
import com.ontop.balance.infrastructure.clients.WalletClient;
import com.ontop.balance.infrastructure.clients.WalletClient.BalanceClientResponse;
import com.ontop.balance.infrastructure.clients.WalletClient.TransactionClientRequest;
import com.ontop.balance.infrastructure.clients.WalletClient.TransactionClientResponse;
import com.ontop.kernels.WalletMessage;
import feign.FeignException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
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
    public void prepareWithdraw(BigDecimal amount, RecipientData recipientData, String transactionId,
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
    public void withdraw(WalletMessage message, BiConsumer<String, TransactionStatus> consumer) {
        consumer.accept(this.getClass().getSimpleName(), TransactionStatus.IN_PROGRESS);
        try {
            this.walletClient.executeTransaction(
                    new TransactionClientRequest(message.getAmount(), message.getClientId()));
            consumer.accept(this.getClass().getSimpleName(), TransactionStatus.COMPLETED);
        } catch (FeignException e) {
            log.error("Transaction >>> {}: {}", message.getTransactionId(), e.getMessage());
            consumer.accept(this.getClass().getSimpleName(), TransactionStatus.FAILED);
        }
    }

    @Override
    public Optional<BalanceData> getBalance(Long clientId) {
        BalanceClientResponse balance = walletClient.getBalance(clientId);
        return Optional.of(new BalanceData(BigDecimal.valueOf(balance.balance())));
    }
}
