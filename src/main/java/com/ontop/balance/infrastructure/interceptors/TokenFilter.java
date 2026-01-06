package com.ontop.balance.infrastructure.interceptors;

import com.ontop.balance.core.model.exceptions.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.IOException;
import java.security.Key;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenFilter implements Filter {

    @Value("${jwt.secret:}")
    private String secretKey;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        MutableHttpRequest mutableHttpRequest = new MutableHttpRequest(req);
        String token = mutableHttpRequest.getHeader(HttpHeaders.AUTHORIZATION);
        if (token != null && token.startsWith("Bearer ")) {
            String jwtToken = token.substring(7).trim();
            if (jwtToken.isEmpty()) {
                log.warn("Empty token after Bearer prefix");
                chain.doFilter(mutableHttpRequest, response);
                return;
            }
            mutableHttpRequest.putHeader("X-Client-Id", getSubject(jwtToken));
        }
        chain.doFilter(mutableHttpRequest, response);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getSubject(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder().setSigningKey(getSignInKey()).build();
            Claims claims = parser.parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (ExpiredJwtException e) {
            log.error("Token has expired", e);
            throw new InvalidTokenException("Token has expired", e);
        } catch (UnsupportedJwtException e) {
            log.error("Token format not supported", e);
            throw new InvalidTokenException("Token format not supported", e);
        } catch (MalformedJwtException e) {
            log.error("Token is malformed", e);
            throw new InvalidTokenException("Token is malformed", e);
        } catch (SignatureException e) {
            log.error("Token signature validation failed", e);
            throw new InvalidTokenException("Token signature validation failed", e);
        } catch (IllegalArgumentException e) {
            log.error("Token is illegal or null", e);
            throw new InvalidTokenException("Token is illegal or null", e);
        }
    }

    private static class MutableHttpRequest extends HttpServletRequestWrapper {

        private final Map<String, String> customHeaders;

        public MutableHttpRequest(HttpServletRequest request) {
            super(request);
            this.customHeaders = new HashMap<>();
        }

        public void putHeader(String key, String value) {
            this.customHeaders.put(key, value);
        }

        @Override
        public Enumeration<String> getHeaders(String key) {

            //TODO: improve this code
            Set<String> headerValues = new HashSet<>();

            String customHeaderValue = customHeaders.get(key);
            if (customHeaderValue != null) {
                headerValues.add(customHeaderValue);
            }

            Enumeration<String> originalHeaders = ((HttpServletRequest) getRequest()).getHeaders(
                    key);
            while (originalHeaders.hasMoreElements()) {
                headerValues.add(originalHeaders.nextElement());
            }

            return Collections.enumeration(headerValues);
        }

        @Override
        public String getHeader(String key) {
            return Optional.ofNullable(customHeaders.get(key))
                    .or(getHeaderFromOriginalWrapper(key, getRequest())).orElse(null);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> set = Stream.concat(customHeaders.keySet().stream(),
                            Collections.list(((HttpServletRequest) getRequest()).getHeaderNames()).stream())
                    .collect(Collectors.toSet());
            return Collections.enumeration(set);
        }

        private static Supplier<Optional<? extends String>> getHeaderFromOriginalWrapper(String key,
                ServletRequest request) {
            return () -> Optional.ofNullable(((HttpServletRequest) request).getHeader(key));
        }

    }
}
