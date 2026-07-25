package com.driftstay.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adds a unique request ID to every request for distributed tracing.
 * If the client sends a X-Request-Id header, it's reused.
 * Otherwise, a new UUID is generated.
 *
 * The request ID is:
 * 1. Set as a response header (X-Request-Id)
 * 2. Added to MDC (Mapped Diagnostic Context) for logging
 * 3. Available in the application for correlation
 */
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_CORRELATION_ID = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Get or generate request ID
            String requestId = request.getHeader(REQUEST_ID_HEADER);
            if (requestId == null || requestId.isBlank()) {
                requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            }

            // Get or generate correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = requestId;
            }

            // Set in MDC for logging
            MDC.put(MDC_REQUEST_ID, requestId);
            MDC.put(MDC_CORRELATION_ID, correlationId);

            // Set response headers
            response.setHeader(REQUEST_ID_HEADER, requestId);
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Store for downstream use
            request.setAttribute(REQUEST_ID_HEADER, requestId);
            request.setAttribute(CORRELATION_ID_HEADER, correlationId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
