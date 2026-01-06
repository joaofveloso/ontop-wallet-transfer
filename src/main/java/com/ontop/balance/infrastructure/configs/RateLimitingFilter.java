package com.ontop.balance.infrastructure.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
     *   <li>Extracts the client IP address</li>
     *   <li>Cleans up old timestamps outside the time window</li>
     *   <li>Checks if the request limit has been exceeded</li>
     *   <li>Either allows the request to proceed or returns HTTP 429</li>
     * </ol>
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

        // Clean old timestamps
        long now = System.currentTimeMillis();
        Queue<Long> timestamps = requestTimestamps.computeIfAbsent(
            clientIp, k -> new ConcurrentLinkedQueue<>()
        );

        // Remove timestamps outside the window
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

        // Add current timestamp
        timestamps.offer(now);

        chain.doFilter(request, response);
    }

    /**
     * Extracts the client IP address from the request.
     *
     * <p>This method first checks for the X-Forwarded-For header, which is set by proxies
     * and load balancers. If present, it uses the first IP address from the header.
     * Otherwise, it falls back to the remote address from the request.</p>
     *
     * <p>The X-Forwarded-For header can contain multiple IP addresses (client, proxy1, proxy2, ...).
     * The first IP address is the original client IP.</p>
     *
     * @param request the HTTP request
     * @return the client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
