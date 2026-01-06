package com.ontop.balance.app.models;

import com.ontop.balance.core.model.queries.ObtainRecipientByClientQuery;
import com.ontop.balance.core.model.queries.ObtainTransactionClientQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for pagination validation in ObtainTransactionClientQuery and ObtainRecipientByClientQuery.
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

    // ==================== ObtainTransactionClientQuery Tests ====================

    @DisplayName("ObtainTransactionClientQuery - Valid page 0 should pass")
    @Test
    void testObtainTransactionClientQueryValidPageZero() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), 0, 10);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with page=0 should pass validation");
    }

    @DisplayName("ObtainTransactionClientQuery - Valid positive page should pass")
    @Test
    void testObtainTransactionClientQueryValidPositivePage() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), 5, 10);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with positive page should pass validation");
    }

    @DisplayName("ObtainTransactionClientQuery - Invalid negative page should fail")
    @Test
    void testObtainTransactionClientQueryInvalidNegativePage() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), -1, 10);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with negative page should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("page")),
            "Should have violation on page field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Page must be 0 or greater")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainTransactionClientQuery - Valid size 1 should pass")
    @Test
    void testObtainTransactionClientQueryValidSizeOne() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), 0, 1);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=1 should pass validation");
    }

    @DisplayName("ObtainTransactionClientQuery - Valid size 100 should pass")
    @Test
    void testObtainTransactionClientQueryValidSizeMax() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), 0, 100);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=100 should pass validation");
    }

    @DisplayName("ObtainTransactionClientQuery - Valid size in range should pass")
    @Test
    void testObtainTransactionClientQueryValidSizeInRange() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), 0, 50);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=50 should pass validation");
    }

    @DisplayName("ObtainTransactionClientQuery - Invalid size 0 should fail")
    @Test
    void testObtainTransactionClientQueryInvalidSizeZero() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), 0, 0);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size=0 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("pageSize")),
            "Should have violation on pageSize field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Size must be at least 1")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainTransactionClientQuery - Invalid negative size should fail")
    @Test
    void testObtainTransactionClientQueryInvalidNegativeSize() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), 0, -10);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with negative size should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("pageSize")),
            "Should have violation on pageSize field");
    }

    @DisplayName("ObtainTransactionClientQuery - Invalid size > 100 should fail")
    @Test
    void testObtainTransactionClientQueryInvalidSizeExceedsMax() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), 0, 101);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size > 100 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("pageSize")),
            "Should have violation on pageSize field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Size cannot exceed 100")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainTransactionClientQuery - Invalid size greatly exceeds max should fail")
    @Test
    void testObtainTransactionClientQueryInvalidSizeGreatlyExceedsMax() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), 0, 1000);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size=1000 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("pageSize")),
            "Should have violation on pageSize field");
    }

    @DisplayName("ObtainTransactionClientQuery - Multiple violations should be reported")
    @Test
    void testObtainTransactionClientQueryMultipleViolations() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), -1, 0);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertEquals(2, violations.size(),
            "Should report 2 violations");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("page")),
            "Should have violation on page field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("pageSize")),
            "Should have violation on pageSize field");
    }

    // ==================== ObtainRecipientByClientQuery Tests ====================

    @DisplayName("ObtainRecipientByClientQuery - Valid page 0 should pass")
    @Test
    void testObtainRecipientByClientQueryValidPageZero() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, 0, 10);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with page=0 should pass validation");
    }

    @DisplayName("ObtainRecipientByClientQuery - Valid positive page should pass")
    @Test
    void testObtainRecipientByClientQueryValidPositivePage() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, 5, 10);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with positive page should pass validation");
    }

    @DisplayName("ObtainRecipientByClientQuery - Invalid negative page should fail")
    @Test
    void testObtainRecipientByClientQueryInvalidNegativePage() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, -1, 10);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with negative page should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("page")),
            "Should have violation on page field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Page must be 0 or greater")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainRecipientByClientQuery - Valid size 1 should pass")
    @Test
    void testObtainRecipientByClientQueryValidSizeOne() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, 0, 1);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=1 should pass validation");
    }

    @DisplayName("ObtainRecipientByClientQuery - Valid size 100 should pass")
    @Test
    void testObtainRecipientByClientQueryValidSizeMax() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, 0, 100);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=100 should pass validation");
    }

    @DisplayName("ObtainRecipientByClientQuery - Valid size in range should pass")
    @Test
    void testObtainRecipientByClientQueryValidSizeInRange() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, 0, 50);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with size=50 should pass validation");
    }

    @DisplayName("ObtainRecipientByClientQuery - Invalid size 0 should fail")
    @Test
    void testObtainRecipientByClientQueryInvalidSizeZero() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, 0, 0);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size=0 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Size must be at least 1")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainRecipientByClientQuery - Invalid negative size should fail")
    @Test
    void testObtainRecipientByClientQueryInvalidNegativeSize() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, 0, -10);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with negative size should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
    }

    @DisplayName("ObtainRecipientByClientQuery - Invalid size > 100 should fail")
    @Test
    void testObtainRecipientByClientQueryInvalidSizeExceedsMax() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, 0, 101);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size > 100 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().equals("Size cannot exceed 100")),
            "Should have correct validation message");
    }

    @DisplayName("ObtainRecipientByClientQuery - Invalid size greatly exceeds max should fail")
    @Test
    void testObtainRecipientByClientQueryInvalidSizeGreatlyExceedsMax() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, 0, 1000);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty(),
            "Request with size=1000 should fail validation");
        assertTrue(violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("size")),
            "Should have violation on size field");
    }

    @DisplayName("ObtainRecipientByClientQuery - Multiple violations should be reported")
    @Test
    void testObtainRecipientByClientQueryMultipleViolations() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, -1, 0);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
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

    @DisplayName("ObtainTransactionClientQuery - Edge case: maximum page number")
    @Test
    void testObtainTransactionClientQueryEdgeCaseMaxPage() {
        ObtainTransactionClientQuery query = new ObtainTransactionClientQuery(1L, LocalDate.now(), Integer.MAX_VALUE, 10);
        Set<ConstraintViolation<ObtainTransactionClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with maximum page value should pass validation");
    }

    @DisplayName("ObtainRecipientByClientQuery - Edge case: maximum page number")
    @Test
    void testObtainRecipientByClientQueryEdgeCaseMaxPage() {
        ObtainRecipientByClientQuery query = new ObtainRecipientByClientQuery(1L, Integer.MAX_VALUE, 10);
        Set<ConstraintViolation<ObtainRecipientByClientQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty(),
            "Request with maximum page value should pass validation");
    }
}
