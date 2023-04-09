package com.ontop.balance.app.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "A response containing a list of transaction items")
public record TransactionResponse(
        @Schema(description = "The list of transaction items") List<TransactionItemResponse> data,
        @Schema(description = "The pagination information for the transaction items") PaginationResponse pagination,
        @Schema(description = "Additional metadata for the response") MetadataResponse meta) {

}