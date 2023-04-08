package com.ontop.balance.app.models;

import java.util.List;

public record TransactionResponse(List<TransactionItemResponse> data, PaginationResponse pagination, MetadataResponse meta) {

}
