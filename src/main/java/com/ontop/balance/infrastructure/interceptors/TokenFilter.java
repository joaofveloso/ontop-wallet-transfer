package com.ontop.balance.infrastructure.interceptors;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

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
            String[] tokenParts = token.split(" ");
            mutableHttpRequest.putHeader( "X-Client-Id", getSubject(tokenParts[1]));
        }
        chain.doFilter(mutableHttpRequest, response);
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getSubject(String token) {
        JwtParser parser = Jwts.parserBuilder().setSigningKey(getSignInKey()).build();
        Claims claims = parser.parseClaimsJws(token).getBody();
        return claims.getSubject();
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
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

            Enumeration<String> originalHeaders = ((HttpServletRequest) getRequest()).getHeaders(key);
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
        private static Supplier<Optional<? extends String>> getHeaderFromOriginalWrapper(
                String key, ServletRequest request) {
            return () -> Optional.ofNullable(((HttpServletRequest) request).getHeader(key));
        }

    }
}
