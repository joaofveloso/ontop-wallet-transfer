package com.ontop.balance.app.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing pagination information")
public record PaginationResponse(@Schema(description = "The current page number") int page,
                                 @Schema(description = "The number of items per page") int pageSize,
                                 @Schema(description = "The total number of pages") int totalPages) {

}
