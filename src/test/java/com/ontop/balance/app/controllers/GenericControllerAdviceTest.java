package com.ontop.balance.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ontop.balance.app.models.ErrorResponse;
import com.ontop.balance.core.model.exceptions.InvalidTokenException;
import com.ontop.balance.core.model.exceptions.UnauthorizedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GenericControllerAdvice Tests")
class GenericControllerAdviceTest {

    private GenericControllerAdvice controllerAdvice;

    @BeforeEach
    void setUp() {
        controllerAdvice = new GenericControllerAdvice();
    }

    @Test
    @DisplayName("InvalidTokenException should return error response with proper structure")
    void testInvalidTokenExceptionReturns403() {
        // Arrange
        String errorMessage = "Invalid JWT token signature";
        InvalidTokenException exception = new InvalidTokenException(errorMessage);

        // Act
        ErrorResponse response = controllerAdvice.handleInvalidTokenException(exception);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("Invalid token", response.message(),
                "Error message should be 'Invalid token'");

        assertNotNull(response.subErrors(), "Sub-errors should not be null");
        assertEquals(1, response.subErrors().size(),
                "Should have exactly one sub-error");

        ErrorResponse.SubErrorResponse subError = response.subErrors().get(0);
        assertEquals("token", subError.key(),
                "Sub-error key should be 'token'");
        assertEquals(errorMessage, subError.message(),
                "Sub-error message should match exception message");
    }

    @Test
    @DisplayName("InvalidTokenException with cause should return proper error response")
    void testInvalidTokenExceptionWithCause() {
        // Arrange
        Throwable cause = new RuntimeException("Malformed token");
        String errorMessage = "Token validation failed";
        InvalidTokenException exception = new InvalidTokenException(errorMessage, cause);

        // Act
        ErrorResponse response = controllerAdvice.handleInvalidTokenException(exception);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("Invalid token", response.message());
        assertEquals(1, response.subErrors().size());
        assertEquals(errorMessage, response.subErrors().get(0).message());
    }

    @Test
    @DisplayName("UnauthorizedException should return error response with proper structure")
    void testUnauthorizedExceptionReturns401() {
        // Arrange
        String errorMessage = "Invalid client credentials";
        UnauthorizedException exception = new UnauthorizedException(errorMessage);

        // Act
        ErrorResponse response = controllerAdvice.handleUnauthorizedException(exception);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("Authentication failed", response.message(),
                "Error message should be 'Authentication failed'");

        assertNotNull(response.subErrors(), "Sub-errors should not be null");
        assertEquals(1, response.subErrors().size(),
                "Should have exactly one sub-error");

        ErrorResponse.SubErrorResponse subError = response.subErrors().get(0);
        assertEquals("authentication", subError.key(),
                "Sub-error key should be 'authentication'");
        assertEquals(errorMessage, subError.message(),
                "Sub-error message should match exception message");
    }

    @Test
    @DisplayName("Error response structure should match existing patterns")
    void testErrorResponseStructureConsistency() {
        // Arrange
        String errorMessage = "Test error message";
        InvalidTokenException exception = new InvalidTokenException(errorMessage);

        // Act
        ErrorResponse response = controllerAdvice.handleInvalidTokenException(exception);

        // Assert - Verify structure matches existing handlers
        assertNotNull(response.message(), "Should have message");
        assertNotNull(response.subErrors(), "Should have sub-errors list");
        assertTrue(response.subErrors().size() > 0, "Should have at least one sub-error");

        ErrorResponse.SubErrorResponse subError = response.subErrors().get(0);
        assertNotNull(subError.key(), "Sub-error should have key");
        assertNotNull(subError.message(), "Sub-error should have message");
    }
}
