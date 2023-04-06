package com.ontop.balance.core.model.commands;

import java.math.BigDecimal;

public record TransferMoneyCommand(String recipientId, Long clientId, BigDecimal amount) {

}
