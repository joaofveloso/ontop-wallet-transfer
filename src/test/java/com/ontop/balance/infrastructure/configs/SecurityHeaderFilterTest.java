package com.ontop.balance.infrastructure.configs;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityHeaderFilterTest {

    private SecurityHeaderFilter securityHeaderFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        securityHeaderFilter = new SecurityHeaderFilter();
    }

    @Test
    void doFilter_AddsXContentTypeOptionsHeader() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/transfers");

        // Act
        securityHeaderFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_AddsXFrameOptionsHeader() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/transfers");

        // Act
        securityHeaderFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setHeader("X-Frame-Options", "DENY");
        verify(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_AddsXXSSProtectionHeader() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/transfers");

        // Act
        securityHeaderFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setHeader("X-XSS-Protection", "1; mode=block");
        verify(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_AddsStrictTransportSecurityHeader() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/transfers");

        // Act
        securityHeaderFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        verify(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_AddsContentSecurityPolicyHeader_NonSwaggerRequest() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/transfers");

        // Act
        securityHeaderFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setHeader("Content-Security-Policy", "default-src 'self'");
        verify(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_DoesNotAddContentSecurityPolicyHeader_SwaggerRequest() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");

        // Act
        securityHeaderFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response, never()).setHeader(eq("Content-Security-Policy"), anyString());
        verify(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_AddsContentSecurityPolicyHeader_SwaggerInPathButNotUI() throws IOException, ServletException {
        // Arrange - URI contains "swagger" but not "swagger-ui"
        when(request.getRequestURI()).thenReturn("/api/swagger-docs");

        // Act
        securityHeaderFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response).setHeader("Content-Security-Policy", "default-src 'self'");
        verify(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_AddsAllSecurityHeaders() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/api/recipients");

        // Act
        securityHeaderFilter.doFilter(request, response, filterChain);

        // Assert - verify all headers are set
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("X-Frame-Options", "DENY");
        verify(response).setHeader("X-XSS-Protection", "1; mode=block");
        verify(response).setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        verify(response).setHeader("Content-Security-Policy", "default-src 'self'");
        verify(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_WithSwaggerUISubPath_DoesNotAddCSP() throws IOException, ServletException {
        // Arrange - swagger-ui with additional path segments
        when(request.getRequestURI()).thenReturn("/swagger-ui/swagger-initializer.js");

        // Act
        securityHeaderFilter.doFilter(request, response, filterChain);

        // Assert
        verify(response, never()).setHeader(eq("Content-Security-Policy"), anyString());
        verify(filterChain).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_AlwaysCallsFilterChain() throws IOException, ServletException {
        // Arrange
        when(request.getRequestURI()).thenReturn("/any-path");

        // Act
        securityHeaderFilter.doFilter(request, response, filterChain);

        // Assert - ensure filter chain is always called
        verify(filterChain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }
}
