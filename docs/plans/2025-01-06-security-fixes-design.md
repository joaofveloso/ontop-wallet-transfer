# Security Fixes Design Document

**Date:** 2025-01-06
**Author:** Claude Code + João Veloso
**Status:** Designed - Ready for Implementation

## Overview

This document describes the design for fixing all 16 security issues identified in the Ontop Wallet Transfer application. The fixes are organized by category and priority, with detailed implementation guidance.

## Critical Issues (Fix Immediately)

### 1. Authentication Bypass - JWT Token Issuance

**Problem:** Current `/login/{clientId}` endpoint issues JWT tokens without any authentication. Anyone can obtain a token for any user ID.

**Location:** `src/main/java/com/ontop/security/TokenController.java:24-33`

**Solution:** Implement clientId + secret authentication.

#### Data Model

**New Entity:**
```java
@Document(collection = "client_credentials")
public class ClientCredentialsEntity {
    @Id
    private Long clientId;

    private String secretHash;  // BCrypt hash
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
}
```

**New Repository:**
```java
public interface ClientCredentialsRepository extends MongoRepository<ClientCredentialsEntity, Long> {
    Optional<ClientCredentialsEntity> findByClientIdAndActiveTrue(Long clientId);
}
```

**New Request DTO:**
```java
public record LoginRequest(
    @NotBlank(message = "Client secret is required")
    String clientSecret
) {}
```

#### Updated Endpoint

**TokenController.java:**
```java
@PostMapping("/login/{clientId}")
public ResponseEntity<Map<String, String>> createJwtToken(
    @PathVariable @Min(1) Long clientId,
    @RequestBody @Valid LoginRequest request,
    HttpServletRequest httpRequest) {

    // Validate credentials
    ClientCredentialsEntity credentials = credentialsRepository
        .findByClientIdAndActiveTrue(clientId)
        .orElseThrow(() -> new UnauthorizedException("Invalid client ID or secret"));

    if (!passwordEncoder.matches(request.clientSecret(), credentials.getSecretHash())) {
        log.warn("Failed login attempt for clientId: {} from IP: {}",
            clientId, httpRequest.getRemoteAddr());
        throw new UnauthorizedException("Invalid client ID or secret");
    }

    // Update last used timestamp
    credentials.setLastUsedAt(LocalDateTime.now());
    credentialsRepository.save(credentials);

    // Issue JWT token
    LocalDateTime now = LocalDateTime.now();
    String token = Jwts.builder()
        .setSubject(String.valueOf(clientId))
        .setIssuedAt(java.sql.Timestamp.valueOf(now))
        .setExpiration(java.sql.Timestamp.valueOf(now.plusHours(expiration)))
        .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();

    return ResponseEntity.ok(Map.of("token", token));
}
```

#### Seed Data

**application-dev.yaml:**
```yaml
app:
  security:
    default-clients:
      - client-id: 123456
        secret: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy  # "secret123"
        active: true
```

### 2. Hardcoded Credentials

**Problem:** MongoDB and JWT secrets hardcoded in `application.yaml`.

**Location:** `src/main/resources/application.yaml:10-13, 41`

**Solution:** Environment variable configuration.

#### Updated Configuration

**application.yaml:**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_USERNAME:ontop_user}:${MONGO_PASSWORD:ontop_pass}@${MONGO_HOST:mongodb}:27017/${MONGO_DB:ontop}?w=majority

jwt:
  secret: ${JWT_SECRET:base64EncodedSecretKeyMustBe256Bits}
  expiration-hours: ${JWT_EXPIRATION_HOURS:24}
```

#### Environment Files

**.env** (gitignored):
```bash
# MongoDB Configuration
MONGO_USERNAME=ontop_user
MONGO_PASSWORD=your_secure_random_password_here
MONGO_HOST=mongodb
MONGO_DB=ontop

# JWT Configuration
JWT_SECRET=your_base64_encoded_256_bit_secret
JWT_EXPIRATION_HOURS=24
```

**.env.example** (committed):
```bash
# MongoDB Configuration
MONGO_USERNAME=your_username
MONGO_PASSWORD=your_secure_password
MONGO_HOST=mongodb
MONGO_DB=ontop

# JWT Configuration
JWT_SECRET=your_base64_encoded_256_bit_secret
JWT_EXPIRATION_HOURS=24
```

#### Generate Secure Secrets

```bash
# Generate MongoDB password
openssl rand -base64 16

# Generate JWT secret (256 bits = 32 bytes, base64 encoded)
openssl rand -base64 32
```

#### Docker Compose Updates

```yaml
services:
  app:
    env_file:
      - .env
    environment:
      - MONGO_USERNAME=${MONGO_USERNAME}
      - MONGO_PASSWORD=${MONGO_PASSWORD}
      - MONGO_HOST=${MONGO_HOST}
      - MONGO_DB=${MONGO_DB}
      - JWT_SECRET=${JWT_SECRET}
      - JWT_EXPIRATION_HOURS=${JWT_EXPIRATION_HOURS}
```

## High Priority Issues

### 3. Input Validation on Path Variables

**Problem:** Path variables accept any value without validation.

**Location:** `src/main/java/com/ontop/security/TokenController.java:24`

**Solution:** Add validation annotations.

```java
@PostMapping("/login/{clientId}")
public ResponseEntity<Map<String, String>> createJwtToken(
    @PathVariable @Min(1) Long clientId,
    @RequestBody @Valid LoginRequest request) {
    // ...
}
```

### 4. JWT Secret Key Validation

**Problem:** Invalid JWT secret causes application crash with poor error message.

**Location:** `src/main/java/com/ontop/security/TokenController.java:35-38`

**Solution:** Validate at application startup.

```java
@PostConstruct
public void validateSecretKey() {
    if (secretKey == null || secretKey.isEmpty()) {
        throw new IllegalStateException(
            "JWT secret key not configured. Set JWT_SECRET environment variable.");
    }
    try {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                "JWT secret key must be at least 256 bits (32 bytes) for HS256. " +
                "Current: " + keyBytes.length + " bytes");
        }
        log.info("JWT secret key validated successfully");
    } catch (IllegalArgumentException e) {
        throw new IllegalStateException(
            "JWT secret key must be valid BASE64 encoded string", e);
    }
}
```

### 5. JWT Token Validation Error Handling

**Problem:** Token parsing throws unhandled exceptions.

**Location:** `src/main/java/com/ontop/balance/infrastructure/interceptors/TokenFilter.java:62-66`

**Solution:** Catch specific JWT exceptions.

```java
private String getSubject(String token) {
    try {
        JwtParser parser = Jwts.parserBuilder()
            .setSigningKey(getSignInKey())
            .build();
        Claims claims = parser.parseClaimsJws(token).getBody();
        return claims.getSubject();
    } catch (ExpiredJwtException e) {
        log.warn("Token expired: {}", e.getMessage());
        throw new InvalidTokenException("Token has expired", e);
    } catch (UnsupportedJwtException e) {
        log.warn("Unsupported token: {}", e.getMessage());
        throw new InvalidTokenException("Token format not supported", e);
    } catch (MalformedJwtException e) {
        log.warn("Malformed token: {}", e.getMessage());
        throw new InvalidTokenException("Token is malformed", e);
    } catch (SignatureException e) {
        log.warn("Invalid token signature: {}", e.getMessage());
        throw new InvalidTokenException("Token signature validation failed", e);
    } catch (IllegalArgumentException e) {
        log.warn("Illegal token argument: {}", e.getMessage());
        throw new InvalidTokenException("Token is illegal or null", e);
    }
}
```

**New Exception:**
```java
package com.ontop.balance.core.model.exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**Exception Handler:**
```java
@ExceptionHandler(InvalidTokenException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public ErrorResponse handleInvalidTokenException(InvalidTokenException exception) {
    log.warn("Token validation failed: {}", exception.getMessage());
    return new ErrorResponse("Authentication failed",
        Collections.singletonList(
            new ErrorResponse.SubErrorResponse("token", exception.getMessage())));
}
```

### 6. Token Parsing Injection Risk

**Problem:** ArrayIndexOutOfBoundsException if token format is malformed.

**Location:** `src/main/java/com/ontop/balance/infrastructure/interceptors/TokenFilter.java:49-53`

**Solution:** Use substring instead of split.

```java
if (token != null && token.startsWith("Bearer ")) {
    String jwtToken = token.substring(7); // Remove "Bearer " prefix
    if (jwtToken.isEmpty()) {
        log.warn("Empty token after Bearer prefix");
        return false;
    }
    try {
        String clientId = getSubject(jwtToken);
        mutableHttpRequest.putHeader("X-Client-Id", clientId);
        return true;
    } catch (InvalidTokenException e) {
        log.warn("Token validation failed: {}", e.getMessage());
        return false;
    }
}
```

### 7. Insufficient Input Validation on Recipient Data

**Problem:** Only @NotBlank validation, no format or length checks.

**Location:** `src/main/java/com/ontop/balance/app/models/CreateRecipientAccountRequest.java`

**Solution:** Add comprehensive validation.

```java
public record CreateRecipientAccountRequest(
    @NotBlank(message = "Routing number is required")
    @Pattern(regexp = "\\d{9}", message = "Routing number must be exactly 9 digits")
    String routingNumber,

    @NotBlank(message = "Account number is required")
    @Size(min = 6, max = 17, message = "Account number must be 6-17 digits")
    @Pattern(regexp = "\\d+", message = "Account number must contain only digits")
    String accountNumber,

    @NotBlank(message = "Identification number is required")
    @Size(min = 8, max = 20, message = "Identification number must be 8-20 characters")
    String identificationNumber,

    @NotBlank(message = "National identification type is required")
    String nationalIdentificationType
) {}
```

### 8. Pagination Bounds Validation

**Problem:** No limits on pagination parameters.

**Location:**
- `src/main/java/com/ontop/balance/app/controllers/TransferMoneyController.java:59-60`
- `src/main/java/com/ontop/balance/app/controllers/RecipientController.java:44-45`

**Solution:** Add validation annotations.

```java
public interface ObtainTransactionQuery {
    @Min(value = 0, message = "Page must be 0 or greater")
    int page();

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    int size();
}
```

## Medium Priority Issues

### 9. Logging Sensitive Information

**Problem:** Full response bodies logged in FeignInterceptor.

**Location:** `src/main/java/com/ontop/balance/infrastructure/interceptors/FeignInterceptor.java:21-22`

**Solution:** Remove body logging.

```java
@Override
public Object aroundDecode(InvocationContext invocationContext) {
    int status = invocationContext.response().status();
    String requestUrl = invocationContext.invocationBuilder().toString();

    // Log only status and URL, NOT the body
    log.info("Feign response - Status: {}, URL: {}", status, requestUrl);

    return invocationContext.proceed();
}
```

### 10. External HTTP Calls Without SSL

**Problem:** External service URL uses HTTP instead of HTTPS.

**Location:** `src/main/resources/application.yaml:38`

**Solution:** Use HTTPS.

```yaml
core:
  wallet:
    client:
      url: ${WALLET_SERVICE_URL:https://mockoon.tools.getontop.com:3000}
```

### 11. Missing Security Headers

**Problem:** No security headers configured.

**Solution:** Add security header filter.

**New Class - SecurityHeaderFilter.java:**
```java
package com.ontop.balance.infrastructure.configs;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Order(1)
public class SecurityHeaderFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Add security headers
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Only add CSP if not serving from Swagger UI
        if (!httpRequest.getRequestURI().contains("/swagger-ui")) {
            httpResponse.setHeader("Content-Security-Policy", "default-src 'self'");
        }

        chain.doFilter(request, response);
    }
}
```

### 12. Rate Limiting Protection

**Problem:** No protection against brute force or DoS attacks.

**Solution:** Implement rate limiting filter.

**New Class - RateLimitingFilter.java:**
```java
package com.ontop.balance.infrastructure.configs;

import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.Queue;

@Component
@Order(2)
public class RateLimitingFilter implements Filter {

    private final Map<String, Queue<Long>> requestTimestamps = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_MS = 60000; // 1 minute

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);

        // Clean old timestamps
        long now = System.currentTimeMillis();
        Queue<Long> timestamps = requestTimestamps.computeIfAbsent(
            clientIp, k -> new ConcurrentLinkedQueue<>()
        );

        // Remove timestamps outside the window
        while (!timestamps.isEmpty() && now - timestamps.peek() > WINDOW_MS) {
            timestamps.poll();
        }

        // Check if limit exceeded
        if (timestamps.size() >= MAX_REQUESTS) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            return;
        }

        // Add current timestamp
        timestamps.offer(now);

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

## Low Priority Issues

### 13. Weak JWT Algorithm Consideration

**Current:** HS256 (HMAC-SHA256)

**Note:** HS256 is secure IF the secret is properly managed. After implementing environment variable secrets, this is acceptable.

**Future enhancement:** Consider migrating to RS256 (asymmetric) for additional security if needed.

## Implementation Order

1. **Setup** (15 minutes)
   - Add .env to .gitignore
   - Create .env.example
   - Update docker-compose.yml

2. **Authentication** (2 hours)
   - Create ClientCredentialsEntity
   - Create ClientCredentialsRepository
   - Update TokenController
   - Add seed data

3. **Configuration** (30 minutes)
   - Update application.yaml
   - Generate secure secrets
   - Add JWT validation @PostConstruct

4. **Input Validation** (1 hour)
   - Update all DTOs with validation annotations
   - Add pagination bounds

5. **JWT Error Handling** (1 hour)
   - Create InvalidTokenException
   - Update TokenFilter
   - Add exception handler

6. **Security Enhancements** (1 hour)
   - Fix FeignInterceptor logging
   - Add SecurityHeaderFilter
   - Add RateLimitingFilter

**Total Estimated Time:** ~6 hours

## Testing Checklist

- [ ] Valid credentials return JWT token
- [ ] Invalid credentials return 401
- [ ] Inactive clients cannot login
- [ ] Missing/invalid JWT secret prevents application startup
- [ ] Expired tokens return 401 with clear message
- [ ] Malformed tokens return 401
- [ ] Invalid signature tokens return 401
- [ ] Rate limiting blocks excessive requests
- [ ] Security headers present in responses
- [ ] Sensitive data not logged
- [ ] Pagination bounds enforced
- [ ] Input validation rejects invalid data

## Rollback Plan

If issues arise:
1. Revert to old authentication temporarily
2. Disable rate limiting by removing filter registration
3. Restore old application.yaml if environment variables break
4. Keep security headers - they don't affect functionality

## References

- Code Review: 2025-01-06 Full Codebase Review
- OWASP Top 10: https://owasp.org/www-project-top-ten/
- JWT Best Practices: https://tools.ietf.org/html/rfc8725
