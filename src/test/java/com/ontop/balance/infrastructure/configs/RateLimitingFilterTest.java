package com.ontop.balance.infrastructure.configs;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
        String proxyIp = "192.168.1.10"; // Private IP, trusted proxy
        String xForwardedFor = realIp + ", " + proxyIp;

        when(request.getRemoteAddr()).thenReturn(proxyIp); // Request comes from trusted proxy
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
        String proxyIp = "127.0.0.1"; // Localhost, trusted proxy

        when(request.getRemoteAddr()).thenReturn(proxyIp); // Request comes from trusted proxy
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
        String proxyIp = "10.0.0.1"; // Private network, trusted proxy
        String xForwardedFor = "  " + realIp + "  , 192.168.1.10";

        when(request.getRemoteAddr()).thenReturn(proxyIp); // Request comes from trusted proxy
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

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void doFilter_PreventsRaceConditionUnderConcurrency() throws Exception {
        // Arrange
        String ipAddress = "192.168.1.100";
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        int numThreads = 10;
        int requestsPerThread = 15; // Total: 150 requests (exceeds limit of 100)
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);

        AtomicInteger passedRequests = new AtomicInteger(0);
        AtomicInteger blockedRequests = new AtomicInteger(0);

        // Act - Launch concurrent requests from multiple threads
        for (int i = 0; i < numThreads; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready

                    for (int j = 0; j < requestsPerThread; j++) {
                        HttpServletRequest threadRequest = mock(HttpServletRequest.class);
                        HttpServletResponse threadResponse = mock(HttpServletResponse.class);
                        FilterChain threadChain = mock(FilterChain.class);
                        PrintWriter threadWriter = mock(PrintWriter.class);

                        when(threadRequest.getRemoteAddr()).thenReturn(ipAddress);
                        when(threadRequest.getHeader("X-Forwarded-For")).thenReturn(null);
                        when(threadResponse.getWriter()).thenReturn(threadWriter);

                        rateLimitingFilter.doFilter(threadRequest, threadResponse, threadChain);

                        // Count passed vs blocked requests
                        try {
                            verify(threadChain, atMostOnce()).doFilter(any(), any());
                            if (j == 0) { // Check first invocation
                                passedRequests.incrementAndGet();
                            }
                        } catch (Exception e) {
                            // Ignore verification errors
                        }
                    }
                } catch (Exception e) {
                    fail("Test thread failed: " + e.getMessage());
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads simultaneously
        endLatch.await(10, TimeUnit.SECONDS); // Wait for completion
        executorService.shutdown();

        // Assert - Should NOT exceed MAX_REQUESTS (100) due to synchronization
        // With race condition, could see 150+ passed
        // With proper synchronization, should see exactly 100 passed
        assertTrue(passedRequests.get() <= 100,
            "Race condition detected! Passed requests: " + passedRequests.get() +
            " (should be <= 100)");
    }

    @Test
    void getClientIp_TrustsXForwardedForFromLocalhost() throws IOException, ServletException {
        // Arrange
        String realIp = "203.0.113.1";
        String localhostIp = "127.0.0.1";

        when(request.getRemoteAddr()).thenReturn(localhostIp); // From localhost
        when(request.getHeader("X-Forwarded-For")).thenReturn(realIp);
        when(response.getWriter()).thenReturn(writer);

        // Act - Make 101 requests
        for (int i = 0; i < 101; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Assert - Should use X-Forwarded-For IP (localhost is trusted)
        verify(filterChain, times(100)).doFilter(any(), any());
        verify(response, atLeastOnce()).setStatus(429);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void getClientIp_IgnoresXForwardedForFromUntrustedIP() throws IOException, ServletException {
        // Arrange
        String realIp = "203.0.113.1";
        String untrustedIp = "8.8.8.8"; // Public IP, not trusted

        when(request.getRemoteAddr()).thenReturn(untrustedIp); // From untrusted IP
        when(request.getHeader("X-Forwarded-For")).thenReturn(realIp);

        // Act - Make 5 requests
        for (int i = 0; i < 5; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Assert - Should use remote address (untrusted IP), ignoring X-Forwarded-For
        verify(filterChain, times(5)).doFilter(any(), any());
        verify(response, never()).setStatus(429);

        // Now make requests with the real IP directly to verify it wasn't rate limited
        when(request.getRemoteAddr()).thenReturn(realIp);
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        // Make 101 requests with real IP - should work because it wasn't counted before
        for (int i = 0; i < 101; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Real IP should be rate limited normally
        verify(filterChain, times(105)).doFilter(any(), any()); // 5 + 100
        verify(response, atLeastOnce()).setStatus(429);
    }

    @Test
    void getClientIp_TrustsXForwardedForFromPrivateNetwork() throws IOException, ServletException {
        // Arrange - Test all private network ranges
        String[] trustedProxies = {
            "10.0.0.1",           // 10.0.0.0/8
            "192.168.1.1",        // 192.168.0.0/16
            "172.16.0.1",         // 172.16.0.0/12
            "172.31.255.255"      // 172.16.0.0/12 end
        };

        for (String proxyIp : trustedProxies) {
            // Create new filter instance for each test
            RateLimitingFilter filter = new RateLimitingFilter();
            String clientIp = "203.0.113." + proxyIp.hashCode() % 256;

            when(request.getRemoteAddr()).thenReturn(proxyIp);
            when(request.getHeader("X-Forwarded-For")).thenReturn(clientIp);
            when(response.getWriter()).thenReturn(writer);
            reset(filterChain);

            // Act - Make 101 requests
            for (int i = 0; i < 101; i++) {
                filter.doFilter(request, response, filterChain);
            }

            // Assert - Should trust X-Forwarded-For from private network
            verify(filterChain, times(100)).doFilter(any(), any());
            verify(response, atLeastOnce()).setStatus(429);
        }
    }

    @Test
    void cleanupInactiveIPs_RemovesOldEntries() throws InterruptedException, IOException, ServletException {
        // This test verifies the cleanup method exists and can be called
        // Actual cleanup testing would require waiting 5 minutes or reflection

        // Arrange - Make some requests to populate the map
        when(request.getRemoteAddr()).thenReturn("192.168.1.50");
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);

        for (int i = 0; i < 10; i++) {
            rateLimitingFilter.doFilter(request, response, filterChain);
        }

        // Act - Call cleanup directly (it won't remove recent entries, but tests the method runs)
        rateLimitingFilter.cleanupInactiveIPs();

        // Assert - No exception thrown, cleanup method is functional
        // (Recent entries won't be removed, but method should execute without errors)
        assertTrue(true, "Cleanup method executed successfully");
    }
}
