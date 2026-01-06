package com.ontop.balance.infrastructure.clients;

import java.math.BigDecimal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import com.ontop.balance.infrastructure.configs.FeignClientConfig;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "wallets", url = "${core.wallet.client.url}",
             configuration = FeignClientConfig.class)
public interface WalletClient {

    @RequestMapping(method = RequestMethod.GET, value = "/wallets/balance", consumes = "application/json")
    BalanceClientResponse getBalance(@RequestParam("user_id") Long userId);

    @RequestMapping(method = RequestMethod.POST, value = "/wallets/transactions", consumes = "application/json")
    TransactionClientResponse executeTransaction(@RequestBody TransactionClientRequest request);

    record BalanceClientResponse(Long balance, Long user_id) {

    }

    record TransactionClientRequest(BigDecimal amount, Long user_id) {

    }

    record TransactionClientResponse(String wallet_transaction_id, BigDecimal amount,
                                     Long user_id) {

    }
}