package com.ontop.balance.core.model.commands;

public record CreateRecipientCommand(
        Long clientId, String name, String routingNumber, String nationalIdentification, String accountNumber) {
}