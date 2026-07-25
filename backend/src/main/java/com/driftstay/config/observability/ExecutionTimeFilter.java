package com.driftstay.config.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Measures and logs execution time for every HTTP request.
 * Adds timing information to MDC for structured logging.
 */
@Slf4j
@Component
@Order(3)
public class ExecutionTimeFilter extends OncePerRequestFilter {

    private static final String MDC_EXECUTION_TIME = "executionTimeMs";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put(MDC_EXECUTION_TIME, String.valueOf(duration));

            // Log slow requests (> 1 second)
            if (duration > 1000) {
                log.warn("SLOW REQUEST: {} {} took {}ms",
                        request.getMethod(), request.getRequestURI(), duration);
            } else {
                log.debug("Request: {} {} completed in {}ms",
                        request.getMethod(), request.getRequestURI(), duration);
            }

            MDC.remove(MDC_EXECUTION_TIME);
        }
    }
}
