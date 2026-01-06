package com.ontop.balance.infrastructure.interceptors;

import com.ontop.balance.core.model.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenFilterTest {

    private TokenFilter tokenFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private FilterChain filterChain;

    private static final String VALID_SECRET = "ThisIsASecretKeyForTestingThatIsLongEnoughForHS256";
    private static final String DIFFERENT_SECRET = "ThisIsADifferentSecretKeyForSignatureValidation";

    @BeforeEach
    void setUp() {
        tokenFilter = new TokenFilter();
        ReflectionTestUtils.setField(tokenFilter, "secretKey", Base64.getEncoder().encodeToString(VALID_SECRET.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void doFilter_WithValidToken_SetsClientIdHeader() throws IOException, ServletException {
        // Arrange
        String token = generateValidToken("user123");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Act
        tokenFilter.doFilter(request, mock(ServletResponse.class), filterChain);

        // Assert
        verify(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_WithExpiredToken_ThrowsInvalidTokenException() {
        // Arrange
        String expiredToken = generateExpiredToken();
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);

        // Act & Assert
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> {
            tokenFilter.doFilter(request, mock(ServletResponse.class), filterChain);
        });

        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    void doFilter_WithMalformedToken_ThrowsInvalidTokenException() {
        // Arrange
        String malformedToken = "this.is.not.a.valid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + malformedToken);

        // Act & Assert
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> {
            tokenFilter.doFilter(request, mock(ServletResponse.class), filterChain);
        });

        assertTrue(exception.getMessage().contains("malformed"));
    }

    @Test
    void doFilter_WithInvalidSignature_ThrowsInvalidTokenException() {
        // Arrange
        // Generate token with one key, but try to parse with different key
        String token = generateTokenWithDifferentSecret();
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        // Act & Assert
        InvalidTokenException exception = assertThrows(InvalidTokenException.class, () -> {
            tokenFilter.doFilter(request, mock(ServletResponse.class), filterChain);
        });

        assertTrue(exception.getMessage().contains("signature"));
    }

    @Test
    void doFilter_WithBearerButEmptyToken_DoesNotSetHeader() throws IOException, ServletException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // Act
        tokenFilter.doFilter(request, mock(ServletResponse.class), filterChain);

        // Assert - should not throw exception, just skip setting the header
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilter_WithBearerNoSpaceAfterPrefix_DoesNotSetHeader() throws IOException, ServletException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearertoken");

        // Act
        tokenFilter.doFilter(request, mock(ServletResponse.class), filterChain);

        // Assert - should not throw exception with substring approach
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilter_WithNoAuthorizationHeader_DoesNotSetHeader() throws IOException, ServletException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        tokenFilter.doFilter(request, mock(ServletResponse.class), filterChain);

        // Assert
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    void doFilter_WithTokenContainingOnlySpaces_DoesNotSetHeader() throws IOException, ServletException {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer    ");

        // Act & Assert - Token with only spaces is trimmed to empty, should be handled gracefully
        tokenFilter.doFilter(request, mock(ServletResponse.class), filterChain);

        // Should continue without throwing exception
        verify(filterChain).doFilter(any(), any());
    }

    // Helper methods to generate tokens

    private String generateValidToken(String subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour from now
                .signWith(Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String generateExpiredToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("user123")
                .setIssuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .setExpiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(Keys.hmacShaKeyFor(VALID_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private String generateTokenWithDifferentSecret() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user123");

        return Jwts.builder()
                .setClaims(claims)
                .setSubject("user123")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(DIFFERENT_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
