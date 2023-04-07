package com.ontop.balance.core.ports.inbound;

import com.ontop.kernels.WalletChargebackMessage;

public interface ExecuteChargebackTransaction {

    void handle(WalletChargebackMessage message);
}
