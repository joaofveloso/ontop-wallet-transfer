package com.ontop.balance.app.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

@Validated
@Schema(description = "Represents a request to transfer money to a recipient account")
public record TransferMoneyRequest(
        @NotBlank
        @Schema(description = "The unique identifier of the recipient account", example = "f8a0e6c2-0c3a-4e8f-bae7-7ecf19a62b6d")
        String recipientId,
        @NotNull
        @DecimalMin(value = "1.00", message = "Transfer amount must be greater than or equal to 1.00")
        @Schema(description = "The amount of money to transfer to the recipient account", example = "1000.00")
        BigDecimal amount
) {}