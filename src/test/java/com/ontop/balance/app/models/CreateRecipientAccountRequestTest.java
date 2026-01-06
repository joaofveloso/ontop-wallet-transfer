package com.ontop.balance.app.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateRecipientAccountRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    // Valid routing number tests
    @Test
    void testValidRoutingNumberNineDigits() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",  // Valid: exactly 9 digits
                "12345678",   // Valid identification: 8 characters
                "987654321",  // Valid account: 9 digits
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Request with valid routing number should pass validation");
    }

    // Invalid routing number tests
    @Test
    void testInvalidRoutingNumberNotNineDigits() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "12345678",   // Invalid: only 8 digits
                "12345678",
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Routing number with less than 9 digits should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("routingNumber")),
                "Should have violation on routingNumber field");
    }

    @Test
    void testInvalidRoutingNumberMoreThanNineDigits() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "1234567890",  // Invalid: 10 digits
                "12345678",
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Routing number with more than 9 digits should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("routingNumber")),
                "Should have violation on routingNumber field");
    }

    @Test
    void testInvalidRoutingNumberContainsNonDigits() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "12345678A",  // Invalid: contains letter
                "12345678",
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Routing number with non-digit characters should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("routingNumber")),
                "Should have violation on routingNumber field");
    }

    @Test
    void testInvalidRoutingNumberContainsSpecialCharacters() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123-456789",  // Invalid: contains dash
                "12345678",
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Routing number with special characters should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("routingNumber")),
                "Should have violation on routingNumber field");
    }

    // Valid account number tests
    @Test
    void testValidAccountNumberSixDigits() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "12345678",
                "123456",     // Valid: exactly 6 digits (minimum)
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Account number with 6 digits should pass validation");
    }

    @Test
    void testValidAccountNumberSeventeenDigits() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "12345678",
                "12345678901234567",  // Valid: exactly 17 digits (maximum)
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Account number with 17 digits should pass validation");
    }

    // Invalid account number tests
    @Test
    void testInvalidAccountNumberTooShort() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "12345678",
                "12345",      // Invalid: only 5 digits (less than minimum 6)
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Account number with less than 6 digits should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")),
                "Should have violation on accountNumber field");
    }

    @Test
    void testInvalidAccountNumberTooLong() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "12345678",
                "123456789012345678",  // Invalid: 18 digits (more than maximum 17)
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Account number with more than 17 digits should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")),
                "Should have violation on accountNumber field");
    }

    @Test
    void testInvalidAccountNumberContainsNonDigits() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "12345678",
                "987654321A",  // Invalid: contains letter
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Account number with non-digit characters should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")),
                "Should have violation on accountNumber field");
    }

    @Test
    void testInvalidAccountNumberContainsSpecialCharacters() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "12345678",
                "987-65-4321",  // Invalid: contains dashes
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Account number with special characters should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("accountNumber")),
                "Should have violation on accountNumber field");
    }

    // Valid identification number tests
    @Test
    void testValidIdentificationNumberEightCharacters() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "12345678",   // Valid: exactly 8 characters (minimum)
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Identification number with 8 characters should pass validation");
    }

    @Test
    void testValidIdentificationNumberTwentyCharacters() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "12345678901234567890",  // Valid: exactly 20 characters (maximum)
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Identification number with 20 characters should pass validation");
    }

    @Test
    void testValidIdentificationNumberWithDashes() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "123-45-6789",  // Valid: 11 characters with dashes
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Identification number with dashes should pass validation");
    }

    // Invalid identification number tests
    @Test
    void testInvalidIdentificationNumberTooShort() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "1234567",    // Invalid: only 7 characters (less than minimum 8)
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Identification number with less than 8 characters should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("identificationNumber")),
                "Should have violation on identificationNumber field");
    }

    @Test
    void testInvalidIdentificationNumberTooLong() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "123456789012345678901",  // Invalid: 21 characters (more than maximum 20)
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Identification number with more than 20 characters should fail validation");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("identificationNumber")),
                "Should have violation on identificationNumber field");
    }

    // Edge case: blank values should still fail with @NotBlank
    @Test
    void testBlankRoutingNumberFailsValidation() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "",           // Invalid: blank
                "12345678",
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Blank routing number should fail validation");
    }

    @Test
    void testBlankAccountNumberFailsValidation() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "12345678",
                "",           // Invalid: blank
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Blank account number should fail validation");
    }

    @Test
    void testBlankIdentificationNumberFailsValidation() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "",           // Invalid: blank
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Blank identification number should fail validation");
    }

    // Edge case: null values should fail with @NotBlank
    @Test
    void testNullRoutingNumberFailsValidation() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                null,         // Invalid: null
                "12345678",
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null routing number should fail validation");
    }

    @Test
    void testNullAccountNumberFailsValidation() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                "12345678",
                null,         // Invalid: null
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null account number should fail validation");
    }

    @Test
    void testNullIdentificationNumberFailsValidation() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
                "John",
                "Doe",
                "123456789",
                null,         // Invalid: null
                "987654321",
                "Bank of Ontop"
        );
        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Null identification number should fail validation");
    }
}
