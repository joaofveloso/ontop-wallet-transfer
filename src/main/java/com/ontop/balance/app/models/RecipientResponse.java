package com.ontop.balance.app.models;

import java.util.List;

public record RecipientResponse(List<RecipientResponseItem> data, PaginationResponse pagination) {

    public record RecipientResponseItem(String id, String name, String routingNumber, String accountNumber) {}
}
