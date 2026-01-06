package com.ontop.security;

import com.ontop.balance.core.model.exceptions.UnauthorizedException;
import com.ontop.balance.infrastructure.entities.ClientCredentialsEntity;
import com.ontop.balance.infrastructure.repositories.ClientCredentialsRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);
    private static final int MIN_SECRET_KEY_BITS = 256;

    private final ClientCredentialsRepository clientCredentialsRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.secret:}")
    private String secretKey;

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

        // Calculate key size in bits
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        int keySizeBits = keyBytes.length * 8;

        if (keySizeBits < MIN_SECRET_KEY_BITS) {
            throw new IllegalStateException(String.format(
                    "JWT secret key is too weak. Current size: %d bits. Minimum required: %d bits. "
                            + "Please use a stronger secret key in jwt.secret configuration.",
                    keySizeBits, MIN_SECRET_KEY_BITS));
        }

        logger.info("JWT secret key validated successfully. Key size: {} bits", keySizeBits);
    }

    @PostMapping("/login/{clientId}")
    public Map<String, String> createJwtToken(
            @PathVariable("clientId") Long clientId,
            @Valid @RequestBody LoginRequest loginRequest) {

        logger.debug("Login attempt for clientId: {}", clientId);

        // Retrieve client credentials
        ClientCredentialsEntity credentials = clientCredentialsRepository
                .findByClientIdAndActiveTrue(clientId)
                .orElseThrow(() -> {
                    logger.warn("Failed login attempt for clientId: {} - Client not found or inactive", clientId);
                    return new UnauthorizedException("Invalid client credentials");
                });

        // Validate client secret
        if (!passwordEncoder.matches(loginRequest.clientSecret(), credentials.getSecretHash())) {
            logger.warn("Failed login attempt for clientId: {} - Invalid secret", clientId);
            throw new UnauthorizedException("Invalid client credentials");
        }

        // Update lastUsedAt timestamp
        credentials.setLastUsedAt(LocalDateTime.now());
        clientCredentialsRepository.save(credentials);

        // Generate JWT token
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plusHours(24); // Use configurable value from application.yaml
        Date issuedAt = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Date expiresAt = Date.from(expirationDate.atZone(ZoneId.systemDefault()).toInstant());

        String token = Jwts.builder()
                .setSubject(String.valueOf(clientId))
                .setIssuedAt(issuedAt)
                .setExpiration(expiresAt)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        logger.info("Successful login for clientId: {}", clientId);

        return Map.of("token", token);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
