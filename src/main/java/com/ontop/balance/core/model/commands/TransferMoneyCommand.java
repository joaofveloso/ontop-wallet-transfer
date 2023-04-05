package com.ontop.balance.core.model.commands;

import java.math.BigDecimal;

public record TransferMoneyCommand(Long recipientId, BigDecimal amount) {

}
