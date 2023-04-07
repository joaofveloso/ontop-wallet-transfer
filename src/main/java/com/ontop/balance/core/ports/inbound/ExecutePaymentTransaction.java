package com.ontop.balance.core.ports.inbound;

import com.ontop.kernels.PaymentMessage;

public interface ExecutePaymentTransaction {

    void handle(PaymentMessage message);
}
