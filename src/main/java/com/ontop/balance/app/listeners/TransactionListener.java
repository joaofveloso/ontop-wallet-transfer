package com.ontop.balance.app.listeners;

import com.ontop.balance.core.ports.inbound.ExecuteChargebackTransaction;
import com.ontop.balance.core.ports.inbound.ExecutePaymentTransaction;
import com.ontop.balance.core.ports.inbound.ExecuteWalletTransaction;
import com.ontop.kernels.ChargebackMessage;
import com.ontop.kernels.ParentMessage;
import com.ontop.kernels.PaymentMessage;
import com.ontop.kernels.WalletMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionListener {

    private final ExecuteWalletTransaction walletTransaction;
    private final ExecutePaymentTransaction paymentTransaction;
    private final ExecuteChargebackTransaction chargebackTransaction;

    @KafkaListener(topics = "${core.topic}", groupId = "core.group", containerFactory = "listenerContainerFactory")
    public void listenToTransactions(ParentMessage parentMessage) {
        if (parentMessage instanceof WalletMessage value) {
            log.info("Wallet transaction: {}", value.getTransactionId());
            this.walletTransaction.handle(value);
        } else if (parentMessage instanceof PaymentMessage value) {
            log.info("Payment transaction: {}", value.getTransactionId());
            this.paymentTransaction.handle(value);
        } else if (parentMessage instanceof ChargebackMessage value) {
            log.warn("Chargeback transaction: {}", value.getTransactionId());
            this.chargebackTransaction.handle(value);
        } else {
            // Publish it to Death Flow
            log.error("ERR");
        }
    }
}
