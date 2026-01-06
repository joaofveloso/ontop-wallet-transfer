package com.ontop.balance.core.model.exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InvalidTokenExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        // Arrange
        String errorMessage = "Invalid JWT token";

        // Act
        InvalidTokenException exception = new InvalidTokenException(errorMessage);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        // Arrange
        String errorMessage = "Invalid JWT token";
        Throwable cause = new RuntimeException("Token expired");

        // Act
        InvalidTokenException exception = new InvalidTokenException(errorMessage, cause);

        // Assert
        assertEquals(errorMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }
}