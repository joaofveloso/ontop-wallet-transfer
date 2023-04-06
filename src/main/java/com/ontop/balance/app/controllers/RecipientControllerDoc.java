package com.ontop.balance.app.controllers;

import com.ontop.balance.app.models.CreateRecipientAccountRequest;
import com.ontop.balance.app.models.RecipientItemWrapper;
import com.ontop.balance.app.models.RecipientResponseWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/recipients")
@SecurityScheme(name = HttpHeaders.AUTHORIZATION, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
@Tag(name = "Recipients", description = "Operations related to creating and obtaining recipient accounts")
public interface RecipientControllerDoc {

    @PostMapping
    @SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
    @Operation(
            summary = "Create a new recipient account",
            description = """
                Creates a new recipient account with the specified account information. 
                The request body should include a JSON object with the recipient account 
                information, including the account name, account type, and initial balance. The 
                server will generate a unique account ID for the new account, which will be 
                returned in the response headers under the `Location` key. If the operation is 
                successful, the response status code will be `201 Created`."""
    )
    ResponseEntity<Void> createRecipient(
            @Parameter(hidden = true) @RequestHeader("X-Client-Id") Long clientId,
            @RequestBody @Valid CreateRecipientAccountRequest request);

    @GetMapping
    @SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
    @Operation(
            summary = "Obtain a paginated list of recipient accounts",
            description = """
                   Returns a paginated list of recipient accounts based on the specified query 
                   parameters. The `page` parameter specifies the page number to retrieve 
                   (starting from 0), and the `size` parameter specifies the number of items per 
                   page. If no query parameters are specified, the default values of `page=0` and 
                   `size=20` will be used. The response body will include a list of recipient 
                   accounts, along with links to the previous and next pages (if available), 
                   as well as metadata about the total number of items and pages."""
    )
    ResponseEntity<RecipientResponseWrapper> obtainRecipients(
            @Parameter(hidden = true) @RequestHeader("X-Client-Id") Long clientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/{id}")
    @SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
    @Operation(
            summary = "Get a recipient account by ID",
            description = """
                    Returns the recipient account with the specified account ID. The `id` parameter 
                    should be a valid UUID that corresponds to an existing recipient account. If 
                    the account is found, the response body will include a JSON object with the 
                    recipient account information, including the account ID, account name, account 
                    type, and account balance. If the account is not found, the response status 
                    code will be `404 Not Found`.""")
    ResponseEntity<RecipientItemWrapper> obtainRecipientById(
            @Parameter(hidden = true) @RequestHeader("X-Client-Id") Long clientId,
            @PathVariable("id") String id);
}
