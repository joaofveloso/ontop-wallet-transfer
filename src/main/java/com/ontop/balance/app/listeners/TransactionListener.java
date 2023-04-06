package com.ontop.balance.app.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontop.kernels.WalletMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionListener {

    private ObjectMapper mapper = new ObjectMapper();

    @KafkaListener(
            topics = "${core.topic}", groupId = "core.group",
            containerFactory = "walletListenerContainerFactory")
    public void listerToTransactions(WalletMessage walletMessage) {

    }
}
