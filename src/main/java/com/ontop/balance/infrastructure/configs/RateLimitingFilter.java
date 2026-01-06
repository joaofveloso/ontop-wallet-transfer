package com.ontop.balance.infrastructure.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Filter that implements rate limiting to protect against DoS and brute force attacks.
 *
 * <p>This filter uses a sliding window algorithm to limit the number of requests from a single IP address.
 * The default configuration allows 100 requests per minute per IP address.</p>
 *
 * <p>The filter:</p>
 * <ul>
 *   <li>Extracts the client IP from the X-Forwarded-For header if present (for proxy scenarios)</li>
 *   <li>Maintains a sliding window of request timestamps per IP</li>
 *   <li>Returns HTTP 429 (Too Many Requests) when the limit is exceeded</li>
 *   <li>Automatically cleans up old timestamps outside the time window</li>
 *   <li>Uses per-IP synchronization to prevent race conditions in check-and-add operations</li>
 *   <li>Periodically cleans up inactive IPs to prevent memory leaks</li>
 *   <li>Validates X-Forwarded-For header only from trusted proxies to prevent IP spoofing</li>
 * </ul>
 *
 * <p><b>Security Measures:</b></p>
 * <ul>
 *   <li><b>Atomic Check-and-Add:</b> Synchronized per-IP queue operations prevent race conditions
 *       where multiple threads could bypass the limit simultaneously</li>
 *   <li><b>Memory Leak Prevention:</b> Scheduled cleanup removes IPs that haven't made requests
 *       in over 2 window periods, preventing unbounded map growth under DoS conditions</li>
 *   <li><b>IP Spoofing Mitigation:</b> X-Forwarded-For header is only trusted from known proxy IPs
 *       (localhost, private networks, load balancers), preventing attackers from spoofing their IP</li>
 * </ul>
 *
 * <p>This filter is ordered with {@literal @}Order(2) to run after the SecurityHeaderFilter.</p>
 *
 * @see javax.servlet.Filter
 */
@Component
@Order(2)
@Slf4j
public class RateLimitingFilter implements Filter {

    /**
     * Map storing request timestamps for each IP address.
     * Uses ConcurrentHashMap for thread-safe operations.
     * Periodically cleaned up to prevent memory leaks.
     */
    private final ConcurrentHashMap<String, Queue<Long>> requestTimestamps = new ConcurrentHashMap<>();

    /**
     * Maximum number of requests allowed per IP within the time window.
     */
    private static final int MAX_REQUESTS = 100;

    /**
     * Time window in milliseconds (1 minute).
     */
    private static final long WINDOW_MS = 60000;

    /**
     * Processes the request through the rate limiting filter.
     *
     * <p>This method:</p>
     * <ol>
     *   <li>Extracts the client IP address (with trusted proxy validation)</li>
     *   <li>Atomically cleans up old timestamps, checks limit, and adds new timestamp</li>
     *   <li>Either allows the request to proceed or returns HTTP 429</li>
     * </ol>
     *
     * <p><b>Thread Safety:</b> The entire check-and-add operation is synchronized per IP
     * to prevent race conditions where multiple threads could simultaneously see 99 requests,
     * both pass the check, and both add, resulting in 101 requests exceeding the limit.</p>
     *
     * @param request  the servlet request
     * @param response the servlet response
     * @param chain    the filter chain
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);

        // Get or create the timestamp queue for this IP
        Queue<Long> timestamps = requestTimestamps.computeIfAbsent(
            clientIp, k -> new ConcurrentLinkedQueue<>()
        );

        // Synchronize per IP to ensure atomic check-and-add
        // This prevents race conditions where multiple threads bypass the limit simultaneously
        synchronized (timestamps) {
            // Clean old timestamps outside the window
            long now = System.currentTimeMillis();
            while (!timestamps.isEmpty() && now - timestamps.peek() > WINDOW_MS) {
                timestamps.poll();
            }

            // Check if limit exceeded
            if (timestamps.size() >= MAX_REQUESTS) {
                httpResponse.setStatus(429); // Too Many Requests
                httpResponse.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
                log.warn("Rate limit exceeded for IP: {}", clientIp);
                return;
            }

            // Add current timestamp (still inside synchronized block for atomicity)
            timestamps.offer(now);
        }

        chain.doFilter(request, response);
    }

    /**
     * Extracts the client IP address from the request with trusted proxy validation.
     *
     * <p>This method implements security measures to prevent IP spoofing:</p>
     * <ol>
     *   <li>Checks if the request comes from a trusted proxy (localhost, private networks)</li>
     *   <li>If from trusted proxy, uses X-Forwarded-For header to get the original client IP</li>
     *   <li>If not from trusted proxy, ignores X-Forwarded-For and uses remote address directly</li>
     * </ol>
     *
     * <p>This prevents attackers from spoofing their IP by sending fake X-Forwarded-For headers.
     * Only requests from known, trusted proxies are allowed to use the X-Forwarded-For header.</p>
     *
     * <p>Trusted proxies include:</p>
     * <ul>
     *   <li>Localhost (127.0.0.1, ::1)</li>
     *   <li>Private network IPs (10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16)</li>
     *   <li>Docker bridge networks (172.17.0.0/16 through 172.31.0.0/16)</li>
     * </ul>
     *
     * @param request the HTTP request
     * @return the client IP address, validated against spoofing
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        // Check if request comes from trusted proxy
        if (!isTrustedProxy(remoteAddr)) {
            // Not from trusted proxy, use remote address directly
            // This prevents attackers from spoofing IPs via X-Forwarded-For
            return remoteAddr;
        }

        // From trusted proxy, check X-Forwarded-For for original client IP
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs: client, proxy1, proxy2, ...
            // The first IP is the original client IP
            return xForwardedFor.split(",")[0].trim();
        }

        // No X-Forwarded-For header, use remote address
        return remoteAddr;
    }

    /**
     * Checks if the given IP address belongs to a trusted proxy.
     *
     * <p>Trusted proxies are typically:</p>
     * <ul>
     *   <li>Localhost (for local development and testing)</li>
     *   <li>Private network IPs (internal network, load balancers)</li>
     *   <li>Docker bridge networks (container-to-container communication)</li>
     * </ul>
     *
     * @param ip the IP address to check
     * @return true if the IP is from a trusted proxy, false otherwise
     */
    private boolean isTrustedProxy(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // Trust localhost
        if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return true;
        }

        // Trust 10.0.0.0/8 (private network)
        if (ip.startsWith("10.")) {
            return true;
        }

        // Trust 192.168.0.0/16 (private network)
        if (ip.startsWith("192.168.")) {
            return true;
        }

        // Trust 172.16.0.0/12 (private network, including Docker bridges)
        // This covers 172.16.0.0 through 172.31.0.0
        if (ip.startsWith("172.16.") || ip.startsWith("172.17.") ||
            ip.startsWith("172.18.") || ip.startsWith("172.19.") ||
            ip.startsWith("172.20.") || ip.startsWith("172.21.") ||
            ip.startsWith("172.22.") || ip.startsWith("172.23.") ||
            ip.startsWith("172.24.") || ip.startsWith("172.25.") ||
            ip.startsWith("172.26.") || ip.startsWith("172.27.") ||
            ip.startsWith("172.28.") || ip.startsWith("172.29.") ||
            ip.startsWith("172.30.") || ip.startsWith("172.31.")) {
            return true;
        }

        return false;
    }

    /**
     * Periodically cleans up inactive IPs from the rate limiting map to prevent memory leaks.
     *
     * <p>This scheduled method runs every 5 minutes and removes IPs that haven't made requests
     * in over 2 window periods (2 minutes). This prevents the map from growing unbounded
     * under DoS conditions with many unique IPs.</p>
     *
     * <p>An IP is considered inactive if:</p>
     * <ul>
     *   <li>Its timestamp queue is empty, OR</li>
     *   <li>The oldest timestamp is older than 2 window periods (2 * WINDOW_MS)</li>
     * </ul>
     *
     * <p>This cleanup is essential for long-running applications to prevent OutOfMemoryError
     * when facing distributed DoS attacks from many different IP addresses.</p>
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes (300000 ms)
    public void cleanupInactiveIPs() {
        long now = System.currentTimeMillis();
        int removed = 0;

        for (Map.Entry<String, Queue<Long>> entry : requestTimestamps.entrySet()) {
            Queue<Long> queue = entry.getValue();

            // Remove IP if queue is empty or oldest timestamp is very old (> 2 window periods)
            if (queue.isEmpty() || (now - queue.peek() > WINDOW_MS * 2)) {
                requestTimestamps.remove(entry.getKey());
                removed++;
            }
        }

        if (removed > 0) {
            log.info("Cleaned up {} inactive IPs from rate limiting map", removed);
        }
    }
}
