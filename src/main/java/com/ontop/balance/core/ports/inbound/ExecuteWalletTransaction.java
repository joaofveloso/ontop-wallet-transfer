package com.ontop.balance.core.ports.inbound;

import com.ontop.kernels.WalletMessage;

public interface ExecuteWalletTransaction {

    void handle(WalletMessage message);
}
