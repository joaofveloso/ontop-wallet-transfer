package com.ontop.balance.app.models;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@Validated
@Schema(description = "Request object for creating a recipient account")
public record CreateRecipientAccountRequest(
        @Schema(description = "The name of the recipient", example = "John") @NotBlank(message = "Name is mandatory") String name,
        @Schema(description = "The lastName of the recipient", example = "John") @NotBlank(message = "Surname is mandatory") String surname,
        @Schema(description = "The routing number of the recipient's bank", example = "123456789") @NotBlank(message = "Routing number is mandatory") String routingNumber,
        @Schema(description = "The national identification number of the recipient", example = "123-45-6789") @NotBlank(message = "Identification number is mandatory") String identificationNumber,
        @Schema(description = "The account number of the recipient", example = "987654321") @NotBlank(message = "Account number is mandatory") String accountNumber,
        @Schema(description = "The name of the recipient's bank", example = "Bank of Ontop") @NotBlank(message = "Bank name is mandatory") String bankName) {

}