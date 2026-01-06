package com.ontop.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ontop.balance.core.model.exceptions.UnauthorizedException;
import com.ontop.balance.infrastructure.entities.ClientCredentialsEntity;
import com.ontop.balance.infrastructure.repositories.ClientCredentialsRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TokenControllerTest {

    @Mock
    private ClientCredentialsRepository clientCredentialsRepository;

    @InjectMocks
    private TokenController tokenController;

    private ClientCredentialsEntity activeClient;
    private ClientCredentialsEntity inactiveClient;
    private String validSecret;
    private String wrongSecret;
    private String validSecretHash;

    @BeforeEach
    void setUp() {
        // Initialize secret key for testing (512 bits = 64 bytes)
        String testSecret = "dGhpc0lzQVZlcnlTdHJvbmcxMjhCaXRTZWNyZXRLZXlGb3JKV1RTaWduaW5nSXRVc2VkSW5UZXN0cw==";
        ReflectionTestUtils.setField(tokenController, "secretKey", testSecret);

        // Initialize password encoder
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();

        // Create test data
        validSecret = "test_secret_123";
        wrongSecret = "wrong_secret_456";
        validSecretHash = passwordEncoder.encode(validSecret);

        // Create active client
        activeClient = new ClientCredentialsEntity();
        activeClient.setClientId(1L);
        activeClient.setSecretHash(validSecretHash);
        activeClient.setActive(true);
        activeClient.setCreatedAt(LocalDateTime.now());

        // Create inactive client
        inactiveClient = new ClientCredentialsEntity();
        inactiveClient.setClientId(2L);
        inactiveClient.setSecretHash(validSecretHash);
        inactiveClient.setActive(false);
        inactiveClient.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Valid credentials should return JWT token")
    void testValidCredentialsReturnToken() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(validSecret);
        Long clientId = activeClient.getClientId();

        when(clientCredentialsRepository.findByClientIdAndActiveTrue(clientId))
                .thenReturn(Optional.of(activeClient));
        when(clientCredentialsRepository.save(any(ClientCredentialsEntity.class)))
                .thenReturn(activeClient);

        // Act
        Map<String, String> response = tokenController.createJwtToken(clientId, loginRequest);

        // Assert
        assertNotNull(response, "Response should not be null");
        assertTrue(response.containsKey("token"), "Response should contain token");
        String token = response.get("token");
        assertNotNull(token, "Token should not be empty");
        assertTrue(token.length() > 100, "JWT token should have reasonable length");

        // Verify save was called to update lastUsedAt
        verify(clientCredentialsRepository).save(activeClient);
    }

    @Test
    @DisplayName("Invalid credentials should return 401")
    void testInvalidCredentialsReturn401() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(wrongSecret);
        Long clientId = activeClient.getClientId();

        when(clientCredentialsRepository.findByClientIdAndActiveTrue(clientId))
                .thenReturn(Optional.of(activeClient));

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> tokenController.createJwtToken(clientId, loginRequest)
        );

        assertEquals("Invalid client credentials", exception.getMessage());

        // Verify save was NOT called
        verify(clientCredentialsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Non-existent client should return 401")
    void testNonExistentClientReturns401() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(validSecret);
        Long nonExistentClientId = 999L;

        when(clientCredentialsRepository.findByClientIdAndActiveTrue(nonExistentClientId))
                .thenReturn(Optional.empty());

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> tokenController.createJwtToken(nonExistentClientId, loginRequest)
        );

        assertEquals("Invalid client credentials", exception.getMessage());

        // Verify save was NOT called
        verify(clientCredentialsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Inactive client should return 401")
    void testInactiveClientReturns401() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(validSecret);
        Long inactiveClientId = inactiveClient.getClientId();

        when(clientCredentialsRepository.findByClientIdAndActiveTrue(inactiveClientId))
                .thenReturn(Optional.empty()); // Inactive clients won't be found

        // Act & Assert
        UnauthorizedException exception = assertThrows(
                UnauthorizedException.class,
                () -> tokenController.createJwtToken(inactiveClientId, loginRequest)
        );

        assertEquals("Invalid client credentials", exception.getMessage());

        // Verify save was NOT called
        verify(clientCredentialsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Multiple successful logins should update lastUsedAt")
    void testMultipleSuccessfulLoginsUpdateLastUsedAt() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest(validSecret);
        Long clientId = activeClient.getClientId();

        when(clientCredentialsRepository.findByClientIdAndActiveTrue(clientId))
                .thenReturn(Optional.of(activeClient));
        when(clientCredentialsRepository.save(any(ClientCredentialsEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act - First login
        tokenController.createJwtToken(clientId, loginRequest);

        // Act - Second login
        tokenController.createJwtToken(clientId, loginRequest);

        // Assert - Should be called twice (once per login)
        verify(clientCredentialsRepository, org.mockito.Mockito.times(2)).save(any(ClientCredentialsEntity.class));
    }

    @Test
    @DisplayName("Should initialize BCryptPasswordEncoder")
    void testBCryptPasswordEncoderInitialization() {
        // This is a structural test to ensure BCryptPasswordEncoder is properly initialized
        // We can verify this indirectly by testing that password matching works
        LoginRequest loginRequest = new LoginRequest(validSecret);
        Long clientId = activeClient.getClientId();

        when(clientCredentialsRepository.findByClientIdAndActiveTrue(clientId))
                .thenReturn(Optional.of(activeClient));
        when(clientCredentialsRepository.save(any(ClientCredentialsEntity.class)))
                .thenReturn(activeClient);

        // Act - should not throw exception
        Map<String, String> response = tokenController.createJwtToken(clientId, loginRequest);

        // Assert
        assertNotNull(response.get("token"));
    }
}
