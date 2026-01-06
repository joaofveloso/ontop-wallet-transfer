package com.ontop.balance.app.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for pagination validation in ObtainTransactionQuery and ObtainRecipientQuery.
 * Ensures pagination parameters are properly validated to prevent excessive resource consumption.
 */
@DisplayName("Pagination Validation Tests")
class PaginationValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ==================== ObtainTransactionQuery Tests ====================

    @DisplayName("ObtainTransactionQuery - Valid page 0 should pass")
    @Test
    void testObtainTransactionQueryValidPageZero() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(0, 10);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with page=0 should pass validation");
    }

    @DisplayName("ObtainTransactionQuery - Valid positive page should pass")
    @Test
    void testObtainTransactionQueryValidPositivePage() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(5, 10);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with positive page should pass validation");
    }

    @DisplayName("ObtainTransactionQuery - Invalid negative page should fail")
    @Test
    void testObtainTransactionQueryInvalidNegativePage() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(-1, 10);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with negative page should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("page")),
            "Should have violation on page field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Page must be 0 or greater")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainTransactionQuery - Valid size 1 should pass")
    @Test
    void testObtainTransactionQueryValidSizeOne() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(0, 1);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=1 should pass validation");
    }

    @DisplayName("ObtainTransactionQuery - Valid size 100 should pass")
    @Test
    void testObtainTransactionQueryValidSizeMax() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(0, 100);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=100 should pass validation");
    }

    @DisplayName("ObtainTransactionQuery - Valid size in range should pass")
    @Test
    void testObtainTransactionQueryValidSizeInRange() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(0, 50);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=50 should pass validation");
    }

    @DisplayName("ObtainTransactionQuery - Invalid size 0 should fail")
    @Test
    void testObtainTransactionQueryInvalidSizeZero() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(0, 0);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size=0 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Size must be at least 1")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainTransactionQuery - Invalid negative size should fail")
    @Test
    void testObtainTransactionQueryInvalidNegativeSize() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(0, -10);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with negative size should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
    }

    @DisplayName("ObtainTransactionQuery - Invalid size > 100 should fail")
    @Test
    void testObtainTransactionQueryInvalidSizeExceedsMax() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(0, 101);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size > 100 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Size cannot exceed 100")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainTransactionQuery - Invalid size greatly exceeds max should fail")
    @Test
    void testObtainTransactionQueryInvalidSizeGreatlyExceedsMax() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(0, 1000);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size=1000 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
    }

    @DisplayName("ObtainTransactionQuery - Multiple violations should be reported")
    @Test
    void testObtainTransactionQueryMultipleViolations() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(-1, 0);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertEquals(2, violations.size(),
            "Should report 2 violations");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("page")),
            "Should have violation on page field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
    }

    // ==================== ObtainRecipientQuery Tests ====================

    @DisplayName("ObtainRecipientQuery - Valid page 0 should pass")
    @Test
    void testObtainRecipientQueryValidPageZero() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(0, 10);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with page=0 should pass validation");
    }

    @DisplayName("ObtainRecipientQuery - Valid positive page should pass")
    @Test
    void testObtainRecipientQueryValidPositivePage() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(5, 10);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with positive page should pass validation");
    }

    @DisplayName("ObtainRecipientQuery - Invalid negative page should fail")
    @Test
    void testObtainRecipientQueryInvalidNegativePage() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(-1, 10);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with negative page should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("page")),
            "Should have violation on page field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Page must be 0 or greater")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainRecipientQuery - Valid size 1 should pass")
    @Test
    void testObtainRecipientQueryValidSizeOne() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(0, 1);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=1 should pass validation");
    }

    @DisplayName("ObtainRecipientQuery - Valid size 100 should pass")
    @Test
    void testObtainRecipientQueryValidSizeMax() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(0, 100);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=100 should pass validation");
    }

    @DisplayName("ObtainRecipientQuery - Valid size in range should pass")
    @Test
    void testObtainRecipientQueryValidSizeInRange() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(0, 50);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=50 should pass validation");
    }

    @DisplayName("ObtainRecipientQuery - Invalid size 0 should fail")
    @Test
    void testObtainRecipientQueryInvalidSizeZero() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(0, 0);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size=0 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Size must be at least 1")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainRecipientQuery - Invalid negative size should fail")
    @Test
    void testObtainRecipientQueryInvalidNegativeSize() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(0, -10);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with negative size should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
    }

    @DisplayName("ObtainRecipientQuery - Invalid size > 100 should fail")
    @Test
    void testObtainRecipientQueryInvalidSizeExceedsMax() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(0, 101);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size > 100 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Size cannot exceed 100")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainRecipientQuery - Invalid size greatly exceeds max should fail")
    @Test
    void testObtainRecipientQueryInvalidSizeGreatlyExceedsMax() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(0, 1000);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size=1000 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
    }

    @DisplayName("ObtainRecipientQuery - Multiple violations should be reported")
    @Test
    void testObtainRecipientQueryMultipleViolations() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(-1, 0);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertEquals(2, violations.size(),
            "Should report 2 violations");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("page")),
            "Should have violation on page field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
    }

    // ==================== Edge Case Tests ====================

    @DisplayName("ObtainTransactionQuery - Edge case: maximum page number")
    @Test
    void testObtainTransactionQueryEdgeCaseMaxPage() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(Integer.MAX_VALUE, 10);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with maximum page value should pass validation");
    }

    @DisplayName("ObtainRecipientQuery - Edge case: maximum page number")
    @Test
    void testObtainRecipientQueryEdgeCaseMaxPage() {
        ObtainRecipientQuery query = new ObtainRecipientQuery(Integer.MAX_VALUE, 10);
        Set<ConstraintViolation<ObtainRecipientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with maximum page value should pass validation");
    }
}
