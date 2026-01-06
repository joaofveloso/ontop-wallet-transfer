package com.ontop.balance.infrastructure.configs;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Security Header Filter that adds security headers to all HTTP responses.
 *
 * <p>This filter implements several HTTP security headers to protect against common web vulnerabilities:</p>
 *
 * <ul>
 *   <li><b>X-Content-Type-Options: nosniff</b> - Prevents MIME-sniffing</li>
 *   <li><b>X-Frame-Options: DENY</b> - Prevents clickjacking attacks</li>
 *   <li><b>X-XSS-Protection: 1; mode=block</b> - Enables XSS filtering</li>
 *   <li><b>Strict-Transport-Security</b> - Enforces HTTPS connections</li>
 *   <li><b>Content-Security-Policy</b> - Controls resource loading (except for Swagger UI)</li>
 * </ul>
 *
 * <p>The Content-Security-Policy header is excluded for Swagger UI requests to prevent
 * breaking the Swagger UI functionality.</p>
 *
 * <p>This filter is ordered with {@code @Order(1)} to ensure it runs before other filters
 * in the chain.</p>
 */
@Component
@Order(1)
public class SecurityHeaderFilter implements Filter {

    /**
     * Adds security headers to all HTTP responses.
     *
     * <p>The following headers are added to all responses:</p>
     * <ul>
     *   <li>X-Content-Type-Options: nosniff</li>
     *   <li>X-Frame-Options: DENY</li>
     *   <li>X-XSS-Protection: 1; mode=block</li>
     *   <li>Strict-Transport-Security: max-age=31536000; includeSubDomains</li>
     *   <li>Content-Security-Policy: default-src 'self' (except for Swagger UI)</li>
     * </ul>
     *
     * @param request  The servlet request
     * @param response The servlet response
     * @param chain    The filter chain
     * @throws IOException      If an I/O error occurs
     * @throws ServletException If a servlet error occurs
     */
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
