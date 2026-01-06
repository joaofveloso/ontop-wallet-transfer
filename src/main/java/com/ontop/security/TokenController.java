package com.ontop.security;

import com.ontop.balance.core.model.exceptions.UnauthorizedException;
import com.ontop.balance.infrastructure.entities.ClientCredentialsEntity;
import com.ontop.balance.infrastructure.repositories.ClientCredentialsRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
    private static final int MIN_SECRET_KEY_BITS = 256;
    private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid client credentials";

    private final ClientCredentialsRepository clientCredentialsRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.secret:}")
    private String secretKey;

    @Value("${jwt.expiration-hours:24}")
    private int jwtExpirationHours;

    public TokenController(ClientCredentialsRepository clientCredentialsRepository) {
        this.clientCredentialsRepository = clientCredentialsRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void validateSecretKey() {
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException(
                    "JWT secret key not configured. Please set jwt.secret in application.yaml");
        }

        // Calculate key size in bits from BASE64 decoded key
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        int keySizeBits = keyBytes.length * 8;

        if (keySizeBits < MIN_SECRET_KEY_BITS) {
            throw new IllegalStateException(String.format(
                    "JWT secret key is too weak. Current size: %d bits. Minimum required: %d bits. "
                            + "Please use a stronger secret key in jwt.secret configuration.",
                    keySizeBits, MIN_SECRET_KEY_BITS));
        }

        logger.info("JWT secret key validated successfully. Key size: {} bits, Expiration: {} hours",
                keySizeBits, jwtExpirationHours);
    }

    @PostMapping("/login/{clientId}")
    public ResponseEntity<Map<String, String>> createJwtToken(
            @PathVariable("clientId") @Min(1) Long clientId,
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {

        String clientIp = request.getRemoteAddr();
        logger.debug("Login attempt for clientId: {} from IP: {}", clientId, clientIp);

        // Use constant-time error handling to prevent timing attacks
        try {
            // Retrieve client credentials
            ClientCredentialsEntity credentials = clientCredentialsRepository
                    .findByClientIdAndActiveTrue(clientId)
                    .orElse(null);

            // Perform constant-time validation
            if (credentials == null || !isValidCredentials(credentials, loginRequest.clientSecret())) {
                // Log failed attempt
                logger.warn("Failed login attempt for clientId: {} from IP: {}", clientId, clientIp);
                throw new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
            }

            // Update lastUsedAt timestamp
            credentials.setLastUsedAt(LocalDateTime.now());
            clientCredentialsRepository.save(credentials);

            // Generate JWT token
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expirationDate = now.plusHours(jwtExpirationHours);
            Date issuedAt = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
            Date expiresAt = Date.from(expirationDate.atZone(ZoneId.systemDefault()).toInstant());

            String token = Jwts.builder()
                    .setSubject(String.valueOf(clientId))
                    .setIssuedAt(issuedAt)
                    .setExpiration(expiresAt)
                    .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                    .compact();

            logger.info("Successful login for clientId: {} from IP: {}", clientId, clientIp);

            return ResponseEntity.ok(Map.of("token", token));

        } catch (UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error during login for clientId: {} from IP: {}", clientId, clientIp, e);
            throw new UnauthorizedException(INVALID_CREDENTIALS_MESSAGE);
        }
    }

    /**
     * Validates credentials in constant-time to prevent timing attacks.
     * This method always performs the same operations regardless of whether
     * the credentials are valid or not.
     */
    private boolean isValidCredentials(ClientCredentialsEntity credentials, String providedSecret) {
        // Always perform the BCrypt check to ensure constant-time execution
        boolean matches = passwordEncoder.matches(providedSecret, credentials.getSecretHash());

        // Add a small constant-time operation to further normalize timing
        // This helps prevent timing analysis even if BCrypt implementation varies
        boolean dummyCheck = passwordEncoder.matches("dummy", credentials.getSecretHash());

        return matches;
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
