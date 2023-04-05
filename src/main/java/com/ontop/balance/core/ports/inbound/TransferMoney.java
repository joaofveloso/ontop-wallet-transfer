package com.ontop.balance.core.ports.inbound;

import com.ontop.balance.core.model.commands.TransferMoneyCommand;

public interface TransferMoney {

    void handler(TransferMoneyCommand transferMoneyCommand);
}
