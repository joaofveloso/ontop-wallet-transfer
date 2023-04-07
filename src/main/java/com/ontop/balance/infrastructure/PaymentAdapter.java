package com.ontop.balance.infrastructure;

import com.ontop.balance.core.model.RecipientData;
import com.ontop.balance.core.model.TransactionData.TransactionStatus;
import com.ontop.balance.core.ports.outbound.Payment;
import com.ontop.balance.infrastructure.clients.PaymentClient;
import com.ontop.balance.infrastructure.clients.PaymentClient.PaymentClientRequest;
import com.ontop.balance.infrastructure.clients.PaymentClient.PaymentClientRequest.AccountData;
import com.ontop.balance.infrastructure.clients.PaymentClient.PaymentClientRequest.DestinationData;
import com.ontop.balance.infrastructure.clients.PaymentClient.PaymentClientRequest.SourceData;
import com.ontop.balance.infrastructure.clients.PaymentClient.PaymentClientRequest.SourceData.SourceType;
import com.ontop.balance.infrastructure.clients.PaymentClient.PaymentClientRequest.SourceInformation;
import com.ontop.kernels.PaymentMessage;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
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

    @Value("${core.ontop.source.name}")
    private String sourceName;
    @Value("${core.ontop.source.account}")
    private String accountNumber;
    @Value("${core.ontop.source.currency}")
    private String currency;
    @Value("${core.ontop.source.routing}")
    private String routingNumber;

    private final KafkaTemplate<String, PaymentMessage> paymentProducer;
    private final PaymentClient paymentClient;

    @Override
    public void prepareTransfer(BigDecimal amount, RecipientData recipientData, String transactionId,
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

    @Override
    public void transfer(PaymentMessage message, BiConsumer<String, TransactionStatus> consumer) {
        consumer.accept(this.getClass().getSimpleName(),TransactionStatus.IN_PROGRESS);
        this.paymentClient.executePayment(toPaymentClientRequest(message));
        consumer.accept(this.getClass().getSimpleName(),TransactionStatus.COMPLETED);
    }

    private PaymentClientRequest toPaymentClientRequest(PaymentMessage paymentMessage) {

        SourceInformation sourceInformation = new SourceInformation(this.sourceName);
        AccountData sourceAccountData = new AccountData(this.accountNumber, this.currency, this.routingNumber);
        SourceData sourceData = new SourceData(SourceType.COMPANY, sourceInformation, sourceAccountData);

        AccountData destinationAccountData = new AccountData(paymentMessage.getAccountNumber(), "USD", paymentMessage.getRoutingNumber());
        DestinationData destinationData = new DestinationData(paymentMessage.getName(), destinationAccountData);

        return new PaymentClientRequest(sourceData, destinationData, paymentMessage.getAmount());
    }

}
