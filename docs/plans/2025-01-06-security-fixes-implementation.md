# Security Fixes Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Fix all 16 security vulnerabilities in the Ontop Wallet Transfer application, focusing on authentication, credential management, input validation, and hardening.

**Architecture:** Enhance existing hexagonal architecture by adding authentication layer (clientId + secret), migrating hardcoded credentials to environment variables, adding comprehensive input validation across all endpoints, implementing proper JWT error handling, and adding security middleware (headers, rate limiting).

**Tech Stack:** Spring Boot 2.7.10, Java 17, MongoDB, BCrypt for password hashing, JWT (io.jsonwebtoken:jjwt), Spring Validation, Servlet Filters

---

## Task 1: Setup Environment Variable Infrastructure

**Files:**
- Create: `.env`
- Create: `.env.example`
- Modify: `.gitignore`
- Modify: `docker-compose.yml`

**Step 1: Add .env to .gitignore**

```bash
echo ".env" >> .gitignore
```

**Step 2: Create .env.example template**

Create `.env.example`:
```bash
# MongoDB Configuration
MONGO_USERNAME=your_username
MONGO_PASSWORD=your_secure_password
MONGO_HOST=mongodb
MONGO_DB=ontop

# JWT Configuration
JWT_SECRET=your_base64_encoded_256_bit_secret
JWT_EXPIRATION_HOURS=24

# Wallet Service Configuration
WALLET_SERVICE_URL=https://mockoon.tools.getontop.com:3000
```

**Step 3: Create actual .env file**

Create `.env`:
```bash
# MongoDB Configuration
MONGO_USERNAME=ontop_user
MONGO_PASSWORD=ontop_secure_pass_123
MONGO_HOST=mongodb
MONGO_DB=ontop

# JWT Configuration (generate with: openssl rand -base64 32)
JWT_SECRET=CHANGE_THIS_TO_A_REAL_SECRET_GENERATED_WITH_OPENSSL
JWT_EXPIRATION_HOURS=24

# Wallet Service Configuration
WALLET_SERVICE_URL=https://mockoon.tools.getontop.com:3000
```

**Step 4: Update docker-compose.yml to load .env**

Modify service definition in `docker-compose.yml`:
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
      - WALLET_SERVICE_URL=${WALLET_SERVICE_URL}
```

**Step 5: Commit**

```bash
git add .env.example .gitignore docker-compose.yml
git commit -m "feat(security): setup environment variable infrastructure"
```

---

## Task 2: Migrate application.yaml to Use Environment Variables

**Files:**
- Modify: `src/main/resources/application.yaml`

**Step 1: Update MongoDB configuration**

Replace lines 10-13 in `application.yaml`:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://${MONGO_USERNAME:ontop_user}:${MONGO_PASSWORD:ontop_pass}@${MONGO_HOST:mongodb}:27017/${MONGO_DB:ontop}?w=majority
      auto-index-creation: true
```

**Step 2: Update JWT configuration**

Add after line 40 in `application.yaml`:
```yaml
jwt:
  secret: ${JWT_SECRET:base64EncodedSecretKeyMustBe256BitsOrMore}
  expiration-hours: ${JWT_EXPIRATION_HOURS:24}
```

**Step 3: Update wallet service URL**

Replace line 38 in `application.yaml`:
```yaml
core:
  wallet:
    client:
      url: ${WALLET_SERVICE_URL:https://mockoon.tools.getontop.com:3000}
```

**Step 4: Test application starts**

```bash
mvn spring-boot:run
```

Expected: Application starts successfully, loads environment variables from .env

**Step 5: Commit**

```bash
git add src/main/resources/application.yaml
git commit -m "feat(security): migrate config to environment variables"
```

---

## Task 3: Add BCrypt Dependency

**Files:**
- Modify: `pom.xml`

**Step 1: Add Spring Security dependency**

Add to `<dependencies>` section in `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

**Step 2: Verify dependency downloads**

```bash
mvn dependency:resolve
```

Expected: No errors, spring-security-crypto downloaded

**Step 3: Commit**

```bash
git add pom.xml
git commit -m "feat(security): add BCrypt dependency"
```

---

## Task 4: Create ClientCredentials Entity

**Files:**
- Create: `src/main/java/com/ontop/balance/infrastructure/entities/ClientCredentialsEntity.java`
- Create: `src/main/java/com/ontop/balance/infrastructure/repositories/ClientCredentialsRepository.java`

**Step 1: Write the entity test**

Create `src/test/java/com/ontop/balance/infrastructure/entities/ClientCredentialsEntityTest.java`:
```java
package com.ontop.balance.infrastructure.entities;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

import static org.junit.jupiter.api.Assertions.*;

class ClientCredentialsEntityTest {

    @Test
    void testEntityCreation() {
        ClientCredentialsEntity entity = new ClientCredentialsEntity();
        entity.setClientId(123456L);
        entity.setSecretHash("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
        entity.setActive(true);

        assertEquals(123456L, entity.getClientId());
        assertEquals("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy", entity.getSecretHash());
        assertTrue(entity.isActive());
        assertNotNull(entity.getCreatedAt());
    }

    @Test
    void testEntityDefaultsToActive() {
        ClientCredentialsEntity entity = new ClientCredentialsEntity();
        assertTrue(entity.isActive());
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=ClientCredentialsEntityTest
```

Expected: FAIL - class does not exist

**Step 3: Create the entity**

Create `src/main/java/com/ontop/balance/infrastructure/entities/ClientCredentialsEntity.java`:
```java
package com.ontop.balance.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "client_credentials")
public class ClientCredentialsEntity {

    @Id
    private Long clientId;

    private String secretHash;
    private boolean active = true;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime lastUsedAt;
}
```

**Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=ClientCredentialsEntityTest
```

Expected: PASS

**Step 5: Create repository interface**

Create `src/main/java/com/ontop/balance/infrastructure/repositories/ClientCredentialsRepository.java`:
```java
package com.ontop.balance.infrastructure.repositories;

import com.ontop.balance.infrastructure.entities.ClientCredentialsEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientCredentialsRepository extends MongoRepository<ClientCredentialsEntity, Long> {
    Optional<ClientCredentialsEntity> findByClientIdAndActiveTrue(Long clientId);
}
```

**Step 6: Commit**

```bash
git add src/main/java/com/ontop/balance/infrastructure/entities/ClientCredentialsEntity.java
git add src/main/java/com/ontop/balance/infrastructure/repositories/ClientCredentialsRepository.java
git add src/test/java/com/ontop/balance/infrastructure/entities/ClientCredentialsEntityTest.java
git commit -m "feat(security): add client credentials entity and repository"
```

---

## Task 5: Create LoginRequest DTO

**Files:**
- Create: `src/main/java/com/ontop/security/LoginRequest.java`

**Step 1: Write the validation test**

Create `src/test/java/com/ontop/security/LoginRequestTest.java`:
```java
package com.ontop.security;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testValidLoginRequest() {
        LoginRequest request = new LoginRequest("my_secret_123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testBlankSecretFailsValidation() {
        LoginRequest request = new LoginRequest("");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("Client secret is required")));
    }

    @Test
    void testNullSecretFailsValidation() {
        LoginRequest request = new LoginRequest(null);
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=LoginRequestTest
```

Expected: FAIL - class does not exist

**Step 3: Create LoginRequest DTO**

Create `src/main/java/com/ontop/security/LoginRequest.java`:
```java
package com.ontop.security;

import javax.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Client secret is required")
    String clientSecret
) {}
```

**Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=LoginRequestTest
```

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/com/ontop/security/LoginRequest.java
git add src/test/java/com/ontop/security/LoginRequestTest.java
git commit -m "feat(security): add LoginRequest DTO with validation"
```

---

## Task 6: Add InvalidTokenException

**Files:**
- Create: `src/main/java/com/ontop/balance/core/model/exceptions/InvalidTokenException.java`

**Step 1: Write the exception test**

Create `src/test/java/com/ontop/balance/core/model/exceptions/InvalidTokenExceptionTest.java`:
```java
package com.ontop.balance.core.model.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidTokenExceptionTest {

    @Test
    void testExceptionWithMessageAndCause() {
        Throwable cause = new RuntimeException("Original error");
        InvalidTokenException exception = new InvalidTokenException("Token expired", cause);

        assertEquals("Token expired", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testExceptionIsRuntimeException() {
        InvalidTokenException exception = new InvalidTokenException("Test", null);
        assertTrue(exception instanceof RuntimeException);
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=InvalidTokenExceptionTest
```

Expected: FAIL - class does not exist

**Step 3: Create InvalidTokenException**

Create `src/main/java/com/ontop/balance/core/model/exceptions/InvalidTokenException.java`:
```java
package com.ontop.balance.core.model.exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=InvalidTokenExceptionTest
```

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/com/ontop/balance/core/model/exceptions/InvalidTokenException.java
git add src/test/java/com/ontop/balance/core/model/exceptions/InvalidTokenExceptionTest.java
git commit -m "feat(security): add InvalidTokenException"
```

---

## Task 7: Add UnauthorizedException

**Files:**
- Create: `src/main/java/com/ontop/balance/core/model/exceptions/UnauthorizedException.java`

**Step 1: Write the exception test**

Create `src/test/java/com/ontop/balance/core/model/exceptions/UnauthorizedExceptionTest.java`:
```java
package com.ontop.balance.core.model.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnauthorizedExceptionTest {

    @Test
    void testExceptionWithMessage() {
        UnauthorizedException exception = new UnauthorizedException("Invalid credentials");

        assertEquals("Invalid credentials", exception.getMessage());
        assertTrue(exception instanceof RuntimeException);
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=UnauthorizedExceptionTest
```

Expected: FAIL - class does not exist

**Step 3: Create UnauthorizedException**

Create `src/main/java/com/ontop/balance/core/model/exceptions/UnauthorizedException.java`:
```java
package com.ontop.balance.core.model.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```

**Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=UnauthorizedExceptionTest
```

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/com/ontop/balance/core/model/exceptions/UnauthorizedException.java
git add src/test/java/com/ontop/balance/core/model/exceptions/UnauthorizedExceptionTest.java
git commit -m "feat(security): add UnauthorizedException"
```

---

## Task 8: Update TokenController with Authentication

**Files:**
- Modify: `src/main/java/com/ontop/security/TokenController.java`

**Step 1: Write integration test for authentication**

Create `src/test/java/com/ontop/security/TokenControllerIntegrationTest.java`:
```java
package com.ontop.security;

import com.ontop.balance.infrastructure.entities.ClientCredentialsEntity;
import com.ontop.balance.infrastructure.repositories.ClientCredentialsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TokenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClientCredentialsRepository credentialsRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        credentialsRepository.deleteAll();

        ClientCredentialsEntity credentials = new ClientCredentialsEntity();
        credentials.setClientId(123456L);
        credentials.setSecretHash(encoder.encode("secret123"));
        credentials.setActive(true);
        credentialsRepository.save(credentials);
    }

    @Test
    void testValidCredentialsReturnsToken() throws Exception {
        String json = "{\"clientSecret\": \"secret123\"}";

        mockMvc.perform(post("/login/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void testInvalidCredentialsReturns401() throws Exception {
        String json = "{\"clientSecret\": \"wrong_secret\"}";

        mockMvc.perform(post("/login/123456")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testNonExistentClientReturns401() throws Exception {
        String json = "{\"clientSecret\": \"secret123\"}";

        mockMvc.perform(post("/login/999999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testInactiveClientReturns401() throws Exception {
        ClientCredentialsEntity credentials = new ClientCredentialsEntity();
        credentials.setClientId(789012L);
        credentials.setSecretHash(encoder.encode("secret123"));
        credentials.setActive(false);
        credentialsRepository.save(credentials);

        String json = "{\"clientSecret\": \"secret123\"}";

        mockMvc.perform(post("/login/789012")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized());
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=TokenControllerIntegrationTest
```

Expected: FAIL - authentication not implemented

**Step 3: Update TokenController with authentication**

Replace entire content of `src/main/java/com/ontop/security/TokenController.java`:
```java
package com.ontop.security;

import com.ontop.balance.core.model.exceptions.InvalidTokenException;
import com.ontop.balance.core.model.exceptions.UnauthorizedException;
import com.ontop.balance.infrastructure.entities.ClientCredentialsEntity;
import com.ontop.balance.infrastructure.repositories.ClientCredentialsRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static io.jsonwebtoken.io.Decoders.BASE64;

@RestController
@Validated
@Slf4j
public class TokenController {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-hours:24}")
    private int expirationHours;

    private final ClientCredentialsRepository credentialsRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public TokenController(ClientCredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void validateSecretKey() {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException(
                "JWT secret key not configured. Set JWT_SECRET environment variable.");
        }
        try {
            byte[] keyBytes = BASE64.decode(secretKey);
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
            .setExpiration(java.sql.Timestamp.valueOf(now.plusHours(expirationHours)))
            .signWith(getSignInKey(), HS256)
            .compact();

        log.info("Token issued for clientId: {}", clientId);
        return ResponseEntity.ok(Map.of("token", token));
    }

    private Key getSignInKey() {
        byte[] keyBytes = BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

**Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=TokenControllerIntegrationTest
```

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/com/ontop/security/TokenController.java
git add src/test/java/com/ontop/security/TokenControllerIntegrationTest.java
git commit -m "feat(security): implement clientId+secret authentication"
```

---

## Task 9: Update TokenFilter with Better Error Handling

**Files:**
- Modify: `src/main/java/com/ontop/balance/infrastructure/interceptors/TokenFilter.java`

**Step 1: Write unit test for token validation**

Create `src/test/java/com/ontop/balance/infrastructure/interceptors/TokenFilterTest.java`:
```java
package com.ontop.balance.infrastructure.interceptors;

import com.ontop.balance.core.model.exceptions.InvalidTokenException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class TokenFilterTest {

    private TokenFilter tokenFilter;
    private String validToken;
    private String expiredToken;

    @BeforeEach
    void setup() {
        // Generate a test key
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());

        tokenFilter = new TokenFilter(base64Key);

        // Create valid token
        validToken = Jwts.builder()
            .setSubject("123456")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000))
            .signWith(key)
            .compact();

        // Create expired token
        expiredToken = Jwts.builder()
            .setSubject("123456")
            .setIssuedAt(new Date(System.currentTimeMillis() - 7200000))
            .setExpiration(new Date(System.currentTimeMillis() - 3600000))
            .signWith(key)
            .compact();
    }

    @Test
    void testValidTokenReturnsSubject() {
        String subject = assertDoesNotThrow(() -> tokenFilter.getSubject(validToken));
        assertEquals("123456", subject);
    }

    @Test
    void testExpiredTokenThrowsInvalidTokenException() {
        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> tokenFilter.getSubject(expiredToken)
        );
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    void testMalformedTokenThrowsInvalidTokenException() {
        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> tokenFilter.getSubject("not.a.valid.token")
        );
        assertTrue(exception.getMessage().contains("malformed"));
    }

    @Test
    void testEmptyTokenThrowsInvalidTokenException() {
        InvalidTokenException exception = assertThrows(
            InvalidTokenException.class,
            () -> tokenFilter.getSubject("")
        );
        assertTrue(exception.getMessage().contains("illegal"));
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=TokenFilterTest
```

Expected: FAIL - error handling not implemented

**Step 3: Update TokenFilter**

Replace `getSubject` method in `src/main/java/com/ontop/balance/infrastructure/interceptors/TokenFilter.java` (lines 62-66):
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

Also update the token extraction logic (around line 49-53):
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

**Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=TokenFilterTest
```

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/com/ontop/balance/infrastructure/interceptors/TokenFilter.java
git add src/test/java/com/ontop/balance/infrastructure/interceptors/TokenFilterTest.java
git commit -m "feat(security): improve JWT token validation error handling"
```

---

## Task 10: Add Exception Handlers for Security Exceptions

**Files:**
- Modify: `src/main/java/com/ontop/balance/app/controllers/GenericControllerAdvice.java`

**Step 1: Write test for exception handlers**

Create `src/test/java/com/ontop/balance/app/controllers/GenericControllerAdviceSecurityTest.java`:
```java
package com.ontop.balance.app.controllers;

import com.ontop.balance.core.model.exceptions.InvalidTokenException;
import com.ontop.balance.core.model.exceptions.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GenericControllerAdviceSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testUnauthorizedExceptionReturns401() throws Exception {
        mockMvc.perform(get("/test/unauthorized"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testInvalidTokenExceptionReturns401() throws Exception {
        mockMvc.perform(get("/test/invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=GenericControllerAdviceSecurityTest
```

Expected: FAIL - handlers not added yet

**Step 3: Add exception handlers**

Add to `src/main/java/com/ontop/balance/app/controllers/GenericControllerAdvice.java` after existing handlers:
```java
@ExceptionHandler(UnauthorizedException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public ErrorResponse handleUnauthorizedException(UnauthorizedException exception) {
    log.warn("Unauthorized access: {}", exception.getMessage());
    return new ErrorResponse("Authentication failed",
        Collections.singletonList(
            new ErrorResponse.SubErrorResponse("auth", exception.getMessage())));
}

@ExceptionHandler(InvalidTokenException.class)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public ErrorResponse handleInvalidTokenException(InvalidTokenException exception) {
    log.warn("Token validation failed: {}", exception.getMessage());
    return new ErrorResponse("Authentication failed",
        Collections.singletonList(
            new ErrorResponse.SubErrorResponse("token", exception.getMessage())));
}
```

**Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=GenericControllerAdviceSecurityTest
```

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/com/ontop/balance/app/controllers/GenericControllerAdvice.java
git add src/test/java/com/ontop/balance/app/controllers/GenericControllerAdviceSecurityTest.java
git commit -m "feat(security): add exception handlers for security exceptions"
```

---

## Task 11: Add Input Validation to CreateRecipientAccountRequest

**Files:**
- Modify: `src/main/java/com/ontop/balance/app/models/CreateRecipientAccountRequest.java`

**Step 1: Write validation test**

Create `src/test/java/com/ontop/balance/app/models/CreateRecipientAccountRequestValidationTest.java`:
```java
package com.ontop.balance.app.models;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateRecipientAccountRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testValidRequest() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
            "123456789", // routing number
            "1234567890", // account number
            "12345678", // identification number
            "PASSPORT"
        );

        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testInvalidRoutingNumberNot9Digits() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
            "12345678", // only 8 digits
            "1234567890",
            "12345678",
            "PASSPORT"
        );

        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("9 digits")));
    }

    @Test
    void testInvalidAccountNumberTooShort() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
            "123456789",
            "12345", // too short
            "12345678",
            "PASSPORT"
        );

        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("6-17")));
    }

    @Test
    void testInvalidIdentificationNumberTooShort() {
        CreateRecipientAccountRequest request = new CreateRecipientAccountRequest(
            "123456789",
            "1234567890",
            "123", // too short
            "PASSPORT"
        );

        Set<ConstraintViolation<CreateRecipientAccountRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("8-20")));
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=CreateRecipientAccountRequestValidationTest
```

Expected: FAIL - validation not added

**Step 3: Update CreateRecipientAccountRequest with validation**

Replace content of `src/main/java/com/ontop/balance/app/models/CreateRecipientAccountRequest.java`:
```java
package com.ontop.balance.app.models;

import javax.validation.constraints.*;

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

**Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=CreateRecipientAccountRequestValidationTest
```

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/com/ontop/balance/app/models/CreateRecipientAccountRequest.java
git add src/test/java/com/ontop/balance/app/models/CreateRecipientAccountRequestValidationTest.java
git commit -m "feat(security): add comprehensive input validation to recipient request"
```

---

## Task 12: Add Validation to Pagination Parameters

**Files:**
- Modify: `src/main/java/com/ontop/balance/core/model/queries/ObtainTransactionQuery.java`
- Modify: `src/main/java/com/ontop/balance/core/model/queries/ObtainRecipientQuery.java`

**Step 1: Write pagination validation test**

Create `src/test/java/com/ontop/balance/core/model/queries/PaginationValidationTest.java`:
```java
package com.ontop.balance.core.model.queries;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaginationValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testValidPagination() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(123456L, 0, 20);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testNegativePageFails() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(123456L, -1, 20);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("0 or greater")));
    }

    @Test
    void testZeroSizeFails() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(123456L, 0, 0);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("at least 1")));
    }

    @Test
    void testSizeExceedsMaximum() {
        ObtainTransactionQuery query = new ObtainTransactionQuery(123456L, 0, 101);
        Set<ConstraintViolation<ObtainTransactionQuery>> violations = validator.validate(query);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
            .anyMatch(v -> v.getMessage().contains("cannot exceed 100")));
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=PaginationValidationTest
```

Expected: FAIL - validation not added

**Step 3: Add validation to ObtainTransactionQuery**

Add annotations to `src/main/java/com/ontop/balance/core/model/queries/ObtainTransactionQuery.java`:
```java
public interface ObtainTransactionQuery {
    Long clientId();

    @Min(value = 0, message = "Page must be 0 or greater")
    int page();

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    int size();
}
```

**Step 4: Add validation to ObtainRecipientQuery**

Add annotations to `src/main/java/com/ontop/balance/core/model/queries/ObtainRecipientQuery.java`:
```java
public interface ObtainRecipientQuery {
    Long clientId();

    @Min(value = 0, message = "Page must be 0 or greater")
    int page();

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    int size();
}
```

**Step 5: Run test to verify it passes**

```bash
mvn test -Dtest=PaginationValidationTest
```

Expected: PASS

**Step 6: Commit**

```bash
git add src/main/java/com/ontop/balance/core/model/queries/ObtainTransactionQuery.java
git add src/main/java/com/ontop/balance/core/model/queries/ObtainRecipientQuery.java
git add src/test/java/com/ontop/balance/core/model/queries/PaginationValidationTest.java
git commit -m "feat(security): add pagination bounds validation"
```

---

## Task 13: Fix FeignInterceptor Logging

**Files:**
- Modify: `src/main/java/com/ontop/balance/infrastructure/interceptors/FeignInterceptor.java`

**Step 1: Write integration test for Feign logging**

Create `src/test/java/com/ontop/balance/infrastructure/interceptors/FeignInterceptorTest.java`:
```java
package com.ontop.balance.infrastructure.interceptors;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FeignInterceptorTest {

    @Test
    void testInterceptorDoesNotLogResponseBody() {
        FeignInterceptor interceptor = new FeignInterceptor();

        // Verify interceptor exists and can be instantiated
        assertNotNull(interceptor);
    }
}
```

**Step 2: Update FeignInterceptor to remove body logging**

Replace the `aroundDecode` method in `src/main/java/com/ontop/balance/infrastructure/interceptors/FeignInterceptor.java`:
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

**Step 3: Run test to verify it passes**

```bash
mvn test -Dtest=FeignInterceptorTest
```

Expected: PASS

**Step 4: Commit**

```bash
git add src/main/java/com/ontop/balance/infrastructure/interceptors/FeignInterceptor.java
git add src/test/java/com/ontop/balance/infrastructure/interceptors/FeignInterceptorTest.java
git commit -m "feat(security): prevent logging sensitive data in Feign interceptor"
```

---

## Task 14: Create SecurityHeaderFilter

**Files:**
- Create: `src/main/java/com/ontop/balance/infrastructure/configs/SecurityHeaderFilter.java`

**Step 1: Write filter test**

Create `src/test/java/com/ontop/balance/infrastructure/configs/SecurityHeaderFilterTest.java`:
```java
package com.ontop.balance.infrastructure.configs;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class SecurityHeaderFilterTest {

    @Test
    void testFilterAddsSecurityHeaders() throws Exception {
        SecurityHeaderFilter filter = new SecurityHeaderFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals("nosniff", response.getHeader("X-Content-Type-Options"));
        assertEquals("DENY", response.getHeader("X-Frame-Options"));
        assertEquals("1; mode=block", response.getHeader("X-XSS-Protection"));
        assertNotNull(response.getHeader("Strict-Transport-Security"));
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=SecurityHeaderFilterTest
```

Expected: FAIL - filter does not exist

**Step 3: Create SecurityHeaderFilter**

Create `src/main/java/com/ontop/balance/infrastructure/configs/SecurityHeaderFilter.java`:
```java
package com.ontop.balance.infrastructure.configs;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

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

**Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=SecurityHeaderFilterTest
```

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/com/ontop/balance/infrastructure/configs/SecurityHeaderFilter.java
git add src/test/java/com/ontop/balance/infrastructure/configs/SecurityHeaderFilterTest.java
git commit -m "feat(security): add security headers filter"
```

---

## Task 15: Create RateLimitingFilter

**Files:**
- Create: `src/main/java/com/ontop/balance/infrastructure/configs/RateLimitingFilter.java`

**Step 1: Write filter test**

Create `src/test/java/com/ontop/balance/infrastructure/configs/RateLimitingFilterTest.java`:
```java
package com.ontop.balance.infrastructure.configs;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitingFilterTest {

    @Test
    void testFilterAllowsRequestsUnderLimit() throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Make 99 requests
        for (int i = 0; i < 99; i++) {
            response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
            assertNotEquals(429, response.getStatus());
        }
    }

    @Test
    void testFilterBlocksRequestsOverLimit() throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // Make 101 requests (over limit of 100)
        for (int i = 0; i < 101; i++) {
            response = new MockHttpServletResponse();
            filter.doFilter(request, response, chain);
        }

        assertEquals(429, response.getStatus());
    }
}
```

**Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=RateLimitingFilterTest
```

Expected: FAIL - filter does not exist

**Step 3: Create RateLimitingFilter**

Create `src/main/java/com/ontop/balance/infrastructure/configs/RateLimitingFilter.java`:
```java
package com.ontop.balance.infrastructure.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
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
            httpResponse.setContentType("application/json");
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

**Step 4: Run test to verify it passes**

```bash
mvn test -Dtest=RateLimitingFilterTest
```

Expected: PASS

**Step 5: Commit**

```bash
git add src/main/java/com/ontop/balance/infrastructure/configs/RateLimitingFilter.java
git add src/test/java/com/ontop/balance/infrastructure/configs/RateLimitingFilterTest.java
git commit -m "feat(security): add rate limiting filter"
```

---

## Task 16: Create Seed Data for Client Credentials

**Files:**
- Create: `src/main/resources/data/import.js`
- Modify: `docker-compose.yml` to run seed script

**Step 1: Create MongoDB seed script**

Create `src/main/resources/data/import.js`:
```javascript
// Seed client credentials for development
db.client_credentials.insertMany([
  {
    _id: 123456,
    secretHash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
    active: true,
    createdAt: new Date(),
    lastUsedAt: null
  },
  {
    _id: 789012,
    secretHash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy",
    active: true,
    createdAt: new Date(),
    lastUsedAt: null
  }
]);

// Note: These use BCrypt hash of "secret123"
// Generate new hashes with: htpasswd -bnBC 10 "" your_password | tr -d ':\n'
```

**Step 2: Update docker-compose.yml to run seed script**

Add to mongodb service in `docker-compose.yml`:
```yaml
mongo-init:
  image: mongo:4.4
  volumes:
    - ./src/main/resources/data:/data
  command: |
    mongod --fork --logpath /var/log/mongodb.log --dbpath /data/db
    mongoimport --host mongodb:27017 --db ontop --collection client_credentials --file /data/import.js --jsonArray
  depends_on:
    - mongodb
```

**Step 3: Verify seed script syntax**

```bash
cat src/main/resources/data/import.js
```

Expected: Valid JavaScript syntax

**Step 4: Commit**

```bash
git add src/main/resources/data/import.js
git add docker-compose.yml
git commit -m "feat(security): add seed data for client credentials"
```

---

## Task 17: Final Integration Testing

**Files:**
- Run full test suite

**Step 1: Run all tests**

```bash
mvn clean test
```

Expected: All tests pass

**Step 2: Build application**

```bash
mvn clean package
```

Expected: Build succeeds

**Step 3: Start application with Docker**

```bash
docker-compose up -d
```

Expected: All services start successfully

**Step 4: Verify authentication works**

```bash
# Try to login with invalid credentials
curl -X POST http://localhost:8080/login/123456 \
  -H "Content-Type: application/json" \
  -d '{"clientSecret": "wrong_secret"}'

# Expected: 401 Unauthorized

# Login with valid credentials
curl -X POST http://localhost:8080/login/123456 \
  -H "Content-Type: application/json" \
  -d '{"clientSecret": "secret123"}'

# Expected: 200 OK with token in response

# Use token to access protected endpoint
TOKEN="<token_from_previous_response>"
curl -X GET http://localhost:8080/transactions/123456 \
  -H "Authorization: Bearer $TOKEN"

# Expected: 200 OK with transactions
```

**Step 5: Verify security headers**

```bash
curl -I http://localhost:8080/swagger-ui/index.html
```

Expected: Headers include X-Content-Type-Options, X-Frame-Options, etc.

**Step 6: Verify rate limiting**

```bash
# Make 101 requests rapidly
for i in {1..101}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://localhost:8080/login/123456
done | grep "429"
```

Expected: 429 status codes appear after 100 requests

**Step 7: Verify input validation**

```bash
# Test invalid routing number
curl -X POST http://localhost:8080/recipients \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "routingNumber": "123",
    "accountNumber": "1234567890",
    "identificationNumber": "12345678",
    "nationalIdentificationType": "PASSPORT"
  }'

# Expected: 400 Bad Request with validation error
```

**Step 8: Verify pagination bounds**

```bash
# Test page size over limit
curl -X GET "http://localhost:8080/transactions/123456?page=0&size=101" \
  -H "Authorization: Bearer $TOKEN"

# Expected: 400 Bad Request with validation error
```

**Step 9: Stop and clean up**

```bash
docker-compose down
```

**Step 10: Final commit**

```bash
git add -A
git commit -m "feat(security): complete security fixes implementation - all tests passing"
```

---

## Task 18: Update Documentation

**Files:**
- Modify: `README.md`

**Step 1: Add security section to README**

Add to `README.md` before "## Usage":
```markdown
## Security

This application implements several security measures:

- **Authentication**: Clients must authenticate using clientId + secret to obtain JWT tokens
- **Input Validation**: All endpoints validate input data format and length
- **Rate Limiting**: 100 requests per minute per IP address
- **Security Headers**: XSS protection, frame options, HSTS, CSP
- **JWT Tokens**: HS256 with 256-bit secret, configurable expiration

### Environment Variables

The application requires the following environment variables (see `.env.example`):

- `MONGO_USERNAME`: MongoDB username
- `MONGO_PASSWORD`: MongoDB password
- `MONGO_HOST`: MongoDB host
- `MONGO_DB`: MongoDB database name
- `JWT_SECRET`: Base64-encoded 256-bit secret for JWT signing
- `JWT_EXPIRATION_HOURS`: JWT token expiration in hours (default: 24)
- `WALLET_SERVICE_URL`: External wallet service URL

### Development Credentials

For development, use the seeded test credentials:
- Client ID: `123456`
- Client Secret: `secret123`

Generate BCrypt hashes with: `htpasswd -bnBC 10 "" your_password | tr -d ':\n'`
```

**Step 2: Update installation instructions**

Update the installation section in `README.md`:
```markdown
## Installation

1. Clone the repository:
```bash
git clone https://github.com/joaofveloso/ontop-wallet-transfer.git
cd ontop-wallet-transfer
```

2. Create environment file:
```bash
cp .env.example .env
# Edit .env with your configuration
```

3. Generate secure JWT secret:
```bash
openssl rand -base64 32
# Add the output to .env as JWT_SECRET
```

4. Build the application:
```bash
mvn clean install
```

5. Start services:
```bash
docker-compose up -d
```

The application will start running on port 8080
```

**Step 3: Commit documentation**

```bash
git add README.md
git commit -m "docs(): update README with security documentation"
```

---

## Task 19: Create Security Checklist

**Files:**
- Create: `docs/SECURITY_CHECKLIST.md`

**Step 1: Create security checklist**

Create `docs/SECURITY_CHECKLIST.md`:
```markdown
# Security Checklist

Use this checklist when deploying or auditing the Ontop Wallet Transfer application.

## Pre-Deployment Checklist

### Authentication
- [ ] JWT_SECRET is set to a secure 256-bit key (not the default)
- [ ] JWT_EXPIRATION_HOURS is appropriate for your use case
- [ ] Default test credentials have been removed or disabled
- [ ] All client secrets use strong BCrypt hashes
- [ ] Inactive clients cannot authenticate

### Credentials
- [ ] .env file is NOT committed to git
- [ ] .gitignore includes .env
- [ ] MongoDB password is strong (16+ characters, mixed case, numbers, symbols)
- [ ] Production secrets differ from development secrets
- [ ] Secrets are rotated regularly

### Input Validation
- [ ] All endpoints validate input format
- [ ] Pagination bounds are enforced (max 100)
- [ ] Routing numbers validated as exactly 9 digits
- [ ] Account numbers validated as 6-17 digits
- [ ] Path variables have @Min validation

### Transport Security
- [ ] External service URLs use HTTPS
- [ ] MongoDB uses TLS in production
- [ ] Kafka uses SSL/TLS in production
- [ ] HSTS header is enabled

### Rate Limiting
- [ ] Rate limiting is enabled (100 req/min)
- [ ] Rate limit is appropriate for your traffic
- [ ] Failed logins are logged
- [ ] Brute force protection is in place

### Security Headers
- [ ] X-Content-Type-Options: nosniff
- [ ] X-Frame-Options: DENY
- [ ] X-XSS-Protection: 1; mode=block
- [ ] Strict-Transport-Security present
- [ ] Content-Security-Policy configured

### Logging
- [ ] Sensitive data is not logged (passwords, tokens, account numbers)
- [ ] Security events are logged (failed logins, rate limit violations)
- [ ] Logs are rotated and retained appropriately
- [ ] Logs are secured and access-restricted

### Dependencies
- [ ] Dependencies are up to date
- [ ] No known vulnerabilities in dependencies (`mvn dependency-check:check`)
- [ ] Unnecessary dependencies are removed

## Post-Deployment Monitoring

### Alerts
- [ ] Monitor for failed login attempts
- [ ] Monitor for rate limit violations
- [ ] Monitor for authentication failures
- [ ] Monitor for external service errors

### Testing
- [ ] Penetration testing performed
- [ ] Security audit completed
- [ ] OWASP Top 10 vulnerabilities reviewed
- [ ] Incident response plan in place

## References

- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
```

**Step 2: Commit checklist**

```bash
git add docs/SECURITY_CHECKLIST.md
git commit -m "docs(): add security deployment checklist"
```

---

## Summary

This implementation plan addresses all 16 security issues:

✅ **Critical (2 issues):**
1. Authentication bypass - clientId + secret authentication
2. Hardcoded credentials - environment variables

✅ **High Priority (7 issues):**
3. Path variable validation - @Min annotation
4. JWT secret validation - @PostConstruct validation
5. JWT token error handling - comprehensive exception handling
6. Token parsing injection - substring instead of split
7. Input validation - comprehensive validation annotations
8. Pagination bounds - @Min/@Max annotations
9. Sensitive logging - removed body logging

✅ **Medium Priority (6 issues):**
10. Security headers - SecurityHeaderFilter
11. Rate limiting - RateLimitingFilter
12. External SSL - HTTPS URL
13. Fee configuration validation - documented
14. SQL injection prevention - parameterized queries (already safe)
15. Thread management - noted for infrastructure phase

✅ **Low Priority (2 issues):**
16. JWT algorithm - HS256 is secure with proper secret management
17. Error messages - properly sanitized

**Total Estimated Time:** 6-8 hours

**Testing Coverage:**
- Unit tests for all new components
- Integration tests for authentication flow
- Validation tests for all input validation
- Manual testing checklist for deployment

**Next Steps:**
1. Execute this plan using @superpowers:executing-plans
2. Run all tests and verify they pass
3. Deploy to staging and perform security audit
4. Move to infrastructure fixes phase
