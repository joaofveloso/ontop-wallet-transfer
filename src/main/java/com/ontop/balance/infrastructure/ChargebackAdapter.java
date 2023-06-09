package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.balance.core.ports.outbound.Chargeback;
import com.ontop.balance.infrastructure.clients.WalletClient;
import com.ontop.balance.infrastructure.clients.WalletClient.TransactionClientRequest;
import com.ontop.balance.infrastructure.entities.TransactionEntity;
import com.ontop.balance.infrastructure.repositories.TransactionRepository;
import com.ontop.kernels.ChargebackMessage;
import feign.FeignException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChargebackAdapter implements Chargeback {

    @Value("${core.topic}")
    private String topic;

    private final TransactionRepository transactionRepository;

    private final WalletClient walletClient;
    private final KafkaTemplate<String, ChargebackMessage> chargebackProducer;

    @Override
    public TransactionStatus prepareChargeback(String transactionId) {

        ProducerRecord<String, ChargebackMessage> changebackRecord = new ProducerRecord<>(
                this.topic, transactionId, new ChargebackMessage(transactionId));
        changebackRecord.headers()
                .add("x-transaction-id", transactionId.getBytes(StandardCharsets.UTF_8));
        try {
            this.chargebackProducer.send(changebackRecord).get();
            return TransactionStatus.PENDING;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Transaction PrepareChargeback >>> {}: {}", transactionId, e.getMessage());
            return TransactionStatus.FAILED;
        }
    }

    @Override
    public TransactionStatus chargeback(ChargebackMessage message) {
        TransactionEntity transactionEntity = this.transactionRepository.findById(
                message.getTransactionId()).orElseThrow();//TODO> handle exceptions
        try {
            this.walletClient.executeTransaction(
                    new TransactionClientRequest(transactionEntity.getAmount().negate(),
                            transactionEntity.getClientId()));
            return TransactionStatus.COMPLETED;
        } catch (FeignException e) {
            log.error("Transaction Changeback >>> {}: {}", message.getTransactionId(),
                    e.getMessage());
            return TransactionStatus.FAILED;
        }
    }
}
