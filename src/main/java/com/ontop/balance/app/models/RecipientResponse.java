package com.ontop.balance.app.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Response object containing a list of recipients and pagination information")
public record RecipientResponse(
        @Schema(description = "The list of recipients") List<RecipientResponseItem> data,
        @Schema(description = "The pagination information") PaginationResponse pagination) {

    @Schema(description = "A recipient item in the response list")
    public record RecipientResponseItem(@Schema(description = "The ID of the recipient") String id,
                                        @Schema(description = "The name of the recipient") String name,
                                        @Schema(description = "The routing number of the recipient's bank") String routingNumber,
                                        @Schema(description = "The account number of the recipient") String accountNumber) {

    }
}
