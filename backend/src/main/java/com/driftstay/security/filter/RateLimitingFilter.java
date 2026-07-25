package com.driftstay.security.filter;

import com.driftstay.util.IpAddressUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter for authentication endpoints.
 * Protects /login, /register, /refresh from brute force and DoS attacks.
 *
 * Uses Bucket4j with an in-memory cache.
 * In production, replace with Redis-backed Bucket4j for distributed rate limiting.
 *
 * Limits:
 * - /login:    5 requests per minute per IP
 * - /register: 3 requests per minute per IP
 * - /refresh:  10 requests per minute per IP
 */
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final IpAddressUtil ipAddressUtil;

    public RateLimitingFilter(IpAddressUtil ipAddressUtil) {
        this.ipAddressUtil = ipAddressUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientIp = ipAddressUtil.getClientIp(request);

        // Only rate-limit auth endpoints
        RateLimitConfig config = getRateLimitConfig(path);
        if (config == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(clientIp + ":" + path, k ->
                Bucket4j.builder()
                        .addLimit(config.bandwidth())
                        .build()
        );

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded for {} from IP {}", path, clientIp);
            String traceId = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/problem+json");
            response.getWriter().write(
                    "{\"type\":\"https://driftstay.com/errors/rate-limited\"," +
                            "\"title\":\"Too Many Requests\"," +
                            "\"status\":429," +
                            "\"detail\":\"Rate limit exceeded. Please try again later.\"," +
                            "\"traceId\":\"" + traceId + "\"," +
                            "\"retryAfterSeconds\":" + config.retryAfterSeconds + "}");
        }
    }

    private RateLimitConfig getRateLimitConfig(String path) {
        if (path.contains("/login")) {
            return new RateLimitConfig(
                    Bandwidth.classic(5, Refill.greedy(5, Duration.ofMinutes(1))),
                    12
            );
        }
        if (path.contains("/register")) {
            return new RateLimitConfig(
                    Bandwidth.classic(3, Refill.greedy(3, Duration.ofMinutes(1))),
                    20
            );
        }
        if (path.contains("/refresh")) {
            return new RateLimitConfig(
                    Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1))),
                    6
            );
        }
        return null;
    }

    private record RateLimitConfig(Bandwidth bandwidth, int retryAfterSeconds) {}
}
