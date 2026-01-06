package com.ontop.balance.app.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.ontop.balance.app.models.ErrorResponse;
import com.ontop.balance.core.model.exceptions.InvalidTokenException;
import com.ontop.balance.core.model.exceptions.UnauthorizedException;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("GenericControllerAdvice Tests")
@ExtendWith(MockitoExtension.class)
class GenericControllerAdviceTest {

    private GenericControllerAdvice controllerAdvice;

    @Mock
    private ConstraintViolation<?> mockViolation;

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
        assertNotNull(response, "Response response should not be null");
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

    @Test
    @DisplayName("ConstraintViolationException should return error response with proper structure")
    void testConstraintViolationExceptionReturns400() {
        // Arrange
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        // Configure mock violation
        when(mockViolation.getMessage()).thenReturn("Page must be 0 or greater");
        when(mockViolation.getPropertyPath()).thenReturn(new javax.validation.Path() {
            @Override
            public String toString() {
                return "obtainRecipients.page";
            }

            @Override
            public java.util.Iterator<Node> iterator() {
                return java.util.Collections.emptyIterator();
            }
        });

        violations.add(mockViolation);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // Act
        ErrorResponse response = controllerAdvice.handleConstraintViolationException(exception);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("Validation failed!", response.message(),
                "Error message should be 'Validation failed!'");

        assertNotNull(response.subErrors(), "Sub-errors should not be null");
        assertEquals(1, response.subErrors().size(),
                "Should have exactly one sub-error");

        ErrorResponse.SubErrorResponse subError = response.subErrors().get(0);
        assertNotNull(subError.key(), "Sub-error key should not be null");
        assertNotNull(subError.message(), "Sub-error message should not be null");
        assertEquals("Page must be 0 or greater", subError.message(),
                "Sub-error message should match violation message");
    }

    @Test
    @DisplayName("ConstraintViolationException with multiple violations should return all errors")
    void testConstraintViolationExceptionWithMultipleViolations() {
        // Arrange
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        // Create first mock violation
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("Size must be at least 1");
        when(violation1.getPropertyPath()).thenReturn(new javax.validation.Path() {
            @Override
            public String toString() {
                return "obtainRecipients.size";
            }

            @Override
            public java.util.Iterator<Node> iterator() {
                return java.util.Collections.emptyIterator();
            }
        });

        // Create second mock violation
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        when(violation2.getMessage()).thenReturn("Page must be 0 or greater");
        when(violation2.getPropertyPath()).thenReturn(new javax.validation.Path() {
            @Override
            public String toString() {
                return "obtainRecipients.page";
            }

            @Override
            public java.util.Iterator<Node> iterator() {
                return java.util.Collections.emptyIterator();
            }
        });

        violations.add(violation1);
        violations.add(violation2);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // Act
        ErrorResponse response = controllerAdvice.handleConstraintViolationException(exception);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertEquals("Validation failed!", response.message());

        assertNotNull(response.subErrors(), "Sub-errors should not be null");
        assertEquals(2, response.subErrors().size(),
                "Should have exactly two sub-errors");

        // Verify both violations are present
        assertTrue(response.subErrors().stream()
                .anyMatch(e -> e.message().equals("Size must be at least 1")),
                "Should contain size validation error");
        assertTrue(response.subErrors().stream()
                .anyMatch(e -> e.message().equals("Page must be 0 or greater")),
                "Should contain page validation error");
    }

    @Test
    @DisplayName("ConstraintViolationException error response should have proper structure")
    void testConstraintViolationExceptionStructureConsistency() {
        // Arrange
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        when(mockViolation.getMessage()).thenReturn("Size cannot exceed 100");
        when(mockViolation.getPropertyPath()).thenReturn(new javax.validation.Path() {
            @Override
            public String toString() {
                return "obtainTransactions.size";
            }

            @Override
            public java.util.Iterator<Node> iterator() {
                return java.util.Collections.emptyIterator();
            }
        });

        violations.add(mockViolation);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // Act
        ErrorResponse response = controllerAdvice.handleConstraintViolationException(exception);

        // Assert - Verify structure matches existing handlers
        assertNotNull(response.message(), "Should have message");
        assertEquals("Validation failed!", response.message(),
                "Message should be 'Validation failed!'");
        assertNotNull(response.subErrors(), "Should have sub-errors list");
        assertTrue(response.subErrors().size() > 0, "Should have at least one sub-error");

        ErrorResponse.SubErrorResponse subError = response.subErrors().get(0);
        assertNotNull(subError.key(), "Sub-error should have key");
        assertNotNull(subError.message(), "Sub-error should have message");
    }
}
