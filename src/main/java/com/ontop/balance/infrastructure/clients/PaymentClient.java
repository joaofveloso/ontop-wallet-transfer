package com.ontop.balance.infrastructure.clients;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
@FeignClient(name = "payments", url = "${core.wallet.client.url}")
public interface PaymentClient {

    @RequestMapping(method = RequestMethod.POST, value = "/api/v1/payments", consumes = "application/json")
    PaymentClientResponse executePayment(@RequestBody PaymentClientRequest request);

    record PaymentClientRequest(SourceData source, DestinationData destination, BigDecimal amount) {

        public record SourceData(SourceType type, SourceInformation sourceInformation,
                                 AccountData account) {

            public enum SourceType {COMPANY, INDIVIDUAL}
        }

        public record SourceInformation(String name) {

        }

        public record AccountData(String accountNumber, String currency, String routingNumber) {

        }

        public record DestinationData(String name, AccountData account) {

        }
    }

    record PaymentClientResponse(PaymentRequestInfoData requestInfo, PaymentInfoData paymentInfo) {

        public record PaymentRequestInfoData(String status) {

        }

        public record PaymentInfoData(BigDecimal amount, String id) {

        }
    }
}
