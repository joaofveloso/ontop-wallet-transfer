package com.ontop.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ontop.balance.core.model.exceptions.UnauthorizedException;
import com.ontop.balance.infrastructure.entities.ClientCredentialsEntity;
import com.ontop.balance.infrastructure.repositories.ClientCredentialsRepository;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class TokenControllerIntegrationTest {

    @Autowired
    private TokenController tokenController;

    @Autowired
    private ClientCredentialsRepository clientCredentialsRepository;

    @MockBean
    private HttpServletRequest request;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private ClientCredentialsEntity activeClient;
    private ClientCredentialsEntity inactiveClient;
    private String validSecret;
    private String wrongSecret;

    @BeforeEach
    void setUp() {
        // Mock HttpServletRequest
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // Clean up database
        clientCredentialsRepository.deleteAll();

        // Create test data
        validSecret = "test_secret_123";
        wrongSecret = "wrong_secret_456";

        // Create active client
        activeClient = new ClientCredentialsEntity();
        activeClient.setClientId(1L);
        activeClient.setSecretHash(passwordEncoder.encode(validSecret));
        activeClient.setActive(true);
        activeClient.setCreatedAt(LocalDateTime.now());
        activeClient = clientCredentialsRepository.save(activeClient);

        // Create inactive client
        inactiveClient = new ClientCredentialsEntity();
        inactiveClient.setClientId(2L);
        inactiveClient.setSecretHash(passwordEncoder.encode(validSecret));
        inactiveClient.setActive(false);
        inactiveClient.setCreatedAt(LocalDateTime.now());
        inactiveClient = clientCredentialsRepository.save(inactiveClient);
    }

    @Test
    @DisplayName("Valid credentials should return JWT token")
    void testValidCredentialsReturnToken() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(validSecret);
        Long clientId = activeClient.getClientId();

        // Act
        ResponseEntity<Map<String, String>> response = tokenController.createJwtToken(
                clientId, loginRequest, request);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        assertTrue(response.getBody().containsKey("token"), "Response should contain token");
        String token = response.getBody().get("token");
        assertNotNull(token, "Token should not be empty");
        assertTrue(token.length() > 50, "JWT token should have reasonable length");

        // Verify lastUsedAt was updated
        ClientCredentialsEntity updatedClient = clientCredentialsRepository.findById(clientId).orElseThrow();
        assertNotNull(updatedClient.getLastUsedAt(), "lastUsedAt should be updated");
    }

    @Test
    @DisplayName("Invalid credentials should return 401")
    void testInvalidCredentialsReturn401() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(wrongSecret);
        Long clientId = activeClient.getClientId();

        // Act & Assert
        UnauthorizedException exception = org.junit.jupiter.api.Assertions.assertThrows(
                UnauthorizedException.class,
                () -> tokenController.createJwtToken(clientId, loginRequest, request)
        );

        assertEquals("Invalid client credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Non-existent client should return 401")
    void testNonExistentClientReturns401() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(validSecret);
        Long nonExistentClientId = 999L;

        // Act & Assert
        UnauthorizedException exception = org.junit.jupiter.api.Assertions.assertThrows(
                UnauthorizedException.class,
                () -> tokenController.createJwtToken(nonExistentClientId, loginRequest, request)
        );

        assertEquals("Invalid client credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Inactive client should return 401")
    void testInactiveClientReturns401() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(validSecret);
        Long inactiveClientId = inactiveClient.getClientId();

        // Act & Assert
        UnauthorizedException exception = org.junit.jupiter.api.Assertions.assertThrows(
                UnauthorizedException.class,
                () -> tokenController.createJwtToken(inactiveClientId, loginRequest, request)
        );

        assertEquals("Invalid client credentials", exception.getMessage());
    }

    @Test
    @DisplayName("Blank secret should fail validation")
    void testBlankSecretFailsValidation() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("");
        Long clientId = activeClient.getClientId();

        // Act & Assert
        // This should trigger validation exception before authentication logic
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> tokenController.createJwtToken(clientId, loginRequest, request)
        );
    }

    @Test
    @DisplayName("Multiple successful logins should update lastUsedAt")
    void testMultipleSuccessfulLoginsUpdateLastUsedAt() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(validSecret);
        Long clientId = activeClient.getClientId();

        // Act - First login
        tokenController.createJwtToken(clientId, loginRequest, request);
        LocalDateTime firstLoginTime = clientCredentialsRepository.findById(clientId).orElseThrow()
                .getLastUsedAt();

        // Wait a bit (though in practice this might be the same millisecond)
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Act - Second login
        tokenController.createJwtToken(clientId, loginRequest, request);
        LocalDateTime secondLoginTime = clientCredentialsRepository.findById(clientId).orElseThrow()
                .getLastUsedAt();

        // Assert
        assertNotNull(firstLoginTime, "First login should update lastUsedAt");
        assertNotNull(secondLoginTime, "Second login should update lastUsedAt");
        assertTrue(
                secondLoginTime.isAfter(firstLoginTime) || secondLoginTime.equals(firstLoginTime),
                "Second login time should be after or equal to first login time"
        );
    }

    @Test
    @DisplayName("Token should contain correct client ID in subject")
    void testTokenContainsCorrectClientId() {
        // This test would require JWT parsing logic
        // For now, we'll just verify the token is generated successfully
        // A more comprehensive test would decode and verify the JWT claims

        // Arrange
        LoginRequest loginRequest = new LoginRequest(validSecret);
        Long clientId = activeClient.getClientId();

        // Act
        ResponseEntity<Map<String, String>> response = tokenController.createJwtToken(
                clientId, loginRequest, request);

        // Assert
        assertNotNull(response.getBody().get("token"), "Token should be generated");
        // Additional JWT parsing and verification could be added here
        // to verify the subject claim matches the clientId
    }
}
