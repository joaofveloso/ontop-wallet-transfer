package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.kernels.PaymentMessage;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentAdapter implements Payment {

    @Value("${core.topic}")
    private String topic;
    private final KafkaTemplate<String, PaymentMessage> paymentProducer;

    @Override
    public void transfer(BigDecimal amount, RecipientData recipientData, String transactionId,
            Consumer<String> stringConsumer) {

        ProducerRecord<String, PaymentMessage> paymentRecord = new ProducerRecord<>(this.topic,
                transactionId, new PaymentMessage(recipientData.clientId(), recipientData.id(),
                recipientData.name(), recipientData.routingNumber(),
                recipientData.nationalIdentification(), recipientData.accountNumber(), amount,transactionId));
        paymentRecord.headers().add("x-transaction-id", transactionId.getBytes(StandardCharsets.UTF_8));
        try {
            var send = this.paymentProducer.send(paymentRecord);
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
}
