package com.ontop.balance.app.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ontop.kernels.ParentMessage;
import com.ontop.kernels.PaymentMessage;
import com.ontop.kernels.WalletChargebackMessage;
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
            containerFactory = "listenerContainerFactory")
    public void listenToTransactions(ParentMessage parentMessage) throws JsonProcessingException {

        if (parentMessage instanceof WalletMessage value) {

            log.info(mapper.writeValueAsString(value));
        } else if (parentMessage instanceof PaymentMessage value) {

            log.info(mapper.writeValueAsString(value));
        } else if (parentMessage instanceof WalletChargebackMessage value) {

            log.info(mapper.writeValueAsString(value));
        } else {
            // Publish it to Death Flow
            log.error("ERR");
        }
    }
}
