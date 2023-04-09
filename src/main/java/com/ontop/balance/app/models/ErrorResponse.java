package com.ontop.balance.app.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Error response object")
public record ErrorResponse(@Schema(description = "The error message") String message,
                            @Schema(description = "A list of sub-error messages") List<SubErrorResponse> subErrors) {

    public record SubErrorResponse(
            @Schema(description = "The key associated with the sub-error") String key,
            @Schema(description = "The sub-error message") String message) {

    }
}