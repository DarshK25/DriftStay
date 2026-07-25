package com.driftstay.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds security headers to every HTTP response.
 * These headers help protect against common web vulnerabilities.
 * Order 1 ensures these are applied before other filters.
 */
@Component
@Order(1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Enable XSS filter in browsers
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Content Security Policy
        response.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' https: data:; " +
                "font-src 'self'; " +
                "connect-src 'self'");

        // Referrer policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions policy (formerly Feature-Policy)
        response.setHeader("Permissions-Policy",
                "camera=(), microphone=(), geolocation=(), payment=()");

        // Strict Transport Security (only in production over HTTPS)
        response.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains");

        // Cache control for sensitive responses
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");

        filterChain.doFilter(request, response);
    }
}
