package com.ontop.balance.core.ports.inbound;

import com.ontop.kernels.ChargebackMessage;

public interface ExecuteChargebackTransaction {

    void handle(ChargebackMessage message);
}
