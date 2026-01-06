package com.ontop.balance.app.models;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

@Validated
@Schema(description = "Request object for creating a recipient account")
public record CreateRecipientAccountRequest(
        @Schema(description = "The name of the recipient", example = "John") @NotBlank(message = "Name is mandatory") String name,
        @Schema(description = "The lastName of the recipient", example = "John") @NotBlank(message = "Surname is mandatory") String surname,
        @Schema(description = "The routing number of the recipient's bank", example = "123456789") @NotBlank(message = "Routing number is required") @Pattern(regexp = "\\d{9}", message = "Routing number must be exactly 9 digits") String routingNumber,
        @Schema(description = "The national identification number of the recipient", example = "123-45-6789") @NotBlank(message = "Identification number is required") @Size(min = 8, max = 20, message = "Identification number must be 8-20 characters") String identificationNumber,
        @Schema(description = "The account number of the recipient", example = "987654321") @NotBlank(message = "Account number is required") @Size(min = 6, max = 17, message = "Account number must be 6-17 digits") @Pattern(regexp = "\\d+", message = "Account number must contain only digits") String accountNumber,
        @Schema(description = "The name of the recipient's bank", example = "Bank of Ontop") @NotBlank(message = "Bank name is mandatory") String bankName) {

}