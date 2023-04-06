package com.ontop.balance.infrastructure.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "wallets", url = "${core.wallet.client.url}")
public interface WalletClient {

    @RequestMapping(method = RequestMethod.GET, value = "/wallets/balance", consumes = "application/json")
    BalanceResponse getBalance(@RequestParam("user_id") Long userId);

    record BalanceResponse(Long balance, Long user_id) {}
}