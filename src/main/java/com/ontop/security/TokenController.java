package com.ontop.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController {

    @Value("${jwt.secret:}")
    private String secretKey;

    @PostMapping("/login/{clientId}")
    public Map<String, String> createJwtToken(@PathVariable("clientId") Long clientId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationDate = now.plusMinutes(30);
        Date issuedAt = Date.from(now.atZone(ZoneId.systemDefault()).toInstant());
        Date expiresAt = Date.from(expirationDate.atZone(ZoneId.systemDefault()).toInstant());
        return Map.of("token",
                Jwts.builder().setSubject(String.valueOf(clientId)).setIssuedAt(issuedAt)
                        .setExpiration(expiresAt).signWith(getSignInKey(), SignatureAlgorithm.HS256)
                        .compact());
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
