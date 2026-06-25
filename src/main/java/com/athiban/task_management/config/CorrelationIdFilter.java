package com.athiban.task_management.config;

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

@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Get or generate correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // 2. Store in MDC (thread-local, accessible by all loggers)
        MDC.put(CORRELATION_ID_KEY, correlationId);

        // 3. Add to response header
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            // 4. Continue to next filter
            filterChain.doFilter(request, response);
        } finally {
            // 5. Clean up MDC (prevent memory leaks in thread pools)
            MDC.remove(CORRELATION_ID_KEY);
        }
    }
}