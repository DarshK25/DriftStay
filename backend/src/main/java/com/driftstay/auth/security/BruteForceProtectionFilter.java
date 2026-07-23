package com.driftstay.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(1)
public class BruteForceProtectionFilter extends OncePerRequestFilter {

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 15 * 60 * 1000L;
    private static final long WINDOW_MS = 60 * 1000L;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!path.contains("/auth/login") && !path.contains("/auth/forgot-password")) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = request.getRemoteAddr();

        if (isBlocked(key)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"success\":false,\"message\":\"Too many login attempts. Try again in 15 minutes.\",\"errorCode\":\"TOO_MANY_ATTEMPTS\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    public void recordFailure(String ip) {
        long now = System.currentTimeMillis();
        attempts.compute(ip, (key, existing) -> {
            if (existing == null || now - existing.windowStart > WINDOW_MS) {
                return new Attempt(1, now);
            }
            existing.count++;
            return existing;
        });
    }

    public void reset(String ip) {
        attempts.remove(ip);
    }

    private boolean isBlocked(String key) {
        long now = System.currentTimeMillis();
        Attempt attempt = attempts.get(key);
        if (attempt == null) return false;
        if (now - attempt.windowStart > WINDOW_MS) {
            attempts.remove(key);
            return false;
        }
        if (now - attempt.windowStart > LOCKOUT_DURATION_MS) {
            attempts.remove(key);
            return false;
        }
        return attempt.count >= MAX_ATTEMPTS;
    }

    public static class Attempt {
        int count;
        long windowStart;
        Attempt(int count, long windowStart) {
            this.count = count;
            this.windowStart = windowStart;
        }
    }
}
