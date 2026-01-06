package com.ontop.balance.infrastructure.configs;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
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
class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
    }

    @Test
    void doFilter_AllowsRequestsUnderLimit() throws IOException, ServletException {
        // Arrange
        String ipAddress = "192.168.1.1";
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        // Act - Make 5 requests (well under the limit of 100)
        for (int i = 0; i < 5; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Assert - All requests should pass through
        verify(filterChain, times(5)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response, never()).setStatus(429);
    }

    @Test
    void doFilter_Returns429WhenLimitExceeded() throws IOException, ServletException {
        // Arrange
        String ipAddress = "192.168.1.2";
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        // Act - Make 101 requests (exceeds limit of 100)
        for (int i = 0; i < 101; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Assert - First 100 should pass, 101st should be blocked
        verify(filterChain, times(100)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response, atLeastOnce()).setStatus(429);
        verify(writer).write("{\"error\": \"Rate limit exceeded\"}");
    }

    @Test
    void doFilter_CleansUpOldTimestamps() throws IOException, ServletException {
        // Arrange
        String ipAddress = "192.168.1.3";
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        // Act - Make 100 requests initially
        for (int i = 0; i < 100; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Wait for window to expire (plus a small buffer)
        try {
            Thread.sleep(61000); // Sleep for 61 seconds (60s window + 1s buffer)
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        // Make one more request - should be allowed since old timestamps are cleaned up
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert - Request should pass after window expires
        verify(filterChain, times(101)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response, never()).setStatus(429);
    }

    @Test
    void doFilter_DifferentIPsHaveIndependentLimits() throws IOException, ServletException {
        // Arrange
        String ip1 = "192.168.1.4";
        String ip2 = "192.168.1.5";

        // Act - Make 100 requests from IP1
        when(request.getRemoteAddr()).thenReturn(ip1);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        for (int i = 0; i < 100; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Make 100 requests from IP2
        when(request.getRemoteAddr()).thenReturn(ip2);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        for (int i = 0; i < 100; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Assert - Both IPs should have independent limits
        verify(filterChain, times(200)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response, never()).setStatus(429);
    }

    @Test
    void doFilter_ExtractsIPFromXForwardedForHeader() throws IOException, ServletException {
        // Arrange
        String realIp = "203.0.113.1";
        String proxyIp = "192.168.1.10";
        String xForwardedFor = realIp + ", " + proxyIp;

        when(request.getHeader("X-Forwarded-For")).thenReturn(xForwardedFor);
        when(response.getWriter()).thenReturn(writer);

        // Act - Make 101 requests with X-Forwarded-For header
        for (int i = 0; i < 101; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Assert - Should track by real IP from X-Forwarded-For
        verify(filterChain, times(100)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response, atLeastOnce()).setStatus(429);
    }

    @Test
    void doFilter_UsesRemoteAddrWhenXForwardedForNotPresent() throws IOException, ServletException {
        // Arrange
        String ipAddress = "192.168.1.6";
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response, never()).setStatus(429);
    }

    @Test
    void doFilter_HandlesEmptyXForwardedForHeader() throws IOException, ServletException {
        // Arrange
        String ipAddress = "192.168.1.7";
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(request.getHeader("X-Forwarded-For")).thenReturn("");

        // Act
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert - Should fall back to remote address
        verify(filterChain, times(1)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response, never()).setStatus(429);
    }

    @Test
    void doFilter_HandlesMultipleIPsInXForwardedFor() throws IOException, ServletException {
        // Arrange
        String firstIp = "203.0.113.10";
        String secondIp = "203.0.113.20";
        String thirdIp = "203.0.113.30";
        String xForwardedFor = firstIp + ", " + secondIp + ", " + thirdIp;

        when(request.getHeader("X-Forwarded-For")).thenReturn(xForwardedFor);
        when(response.getWriter()).thenReturn(writer);

        // Act - Make 101 requests
        for (int i = 0; i < 101; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Assert - Should use first IP from X-Forwarded-For
        verify(filterChain, times(100)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response, atLeastOnce()).setStatus(429);
    }

    @Test
    void doFilter_AllowsRequestAfterWindowExpiry() throws IOException, ServletException {
        // Arrange
        String ipAddress = "192.168.1.8";
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        // Act - Make 100 requests to fill the window
        for (int i = 0; i < 100; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Verify 101st is blocked
        rateLimitingFilter.doFilter(request, response, filterChain);
        verify(response, atLeastOnce()).setStatus(429);

        // Wait for window to expire
        try {
            Thread.sleep(61000);
        } catch (InterruptedException e) {
            fail("Test interrupted");
        }

        // Make a new request - should be allowed
        rateLimitingFilter.doFilter(request, response, filterChain);

        // Assert - Request should pass after window expiry
        verify(filterChain, times(101)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void doFilter_TrimsWhitespaceFromXForwardedFor() throws IOException, ServletException {
        // Arrange
        String realIp = "203.0.113.50";
        String xForwardedFor = "  " + realIp + "  , 192.168.1.10";

        when(request.getHeader("X-Forwarded-For")).thenReturn(xForwardedFor);
        when(response.getWriter()).thenReturn(writer);

        // Act - Make 101 requests
        for (int i = 0; i < 101; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Assert - Should handle whitespace correctly
        verify(filterChain, times(100)).doFilter(any(ServletRequest.class), any(ServletResponse.class));
        verify(response, atLeastOnce()).setStatus(429);
    }
}
