package com.example.bank.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();

        String requestId = request.getHeader("X-Request-Id");

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        MDC.put("requestId", requestId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());

        response.setHeader("X-Request-Id", requestId);

        try {
            chain.doFilter(request, response);

            Authentication auth =
                    SecurityContextHolder.getContext().getAuthentication();

            if (auth != null
                    && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {

                MDC.put("userId", auth.getName());
            }

        } finally {

            long duration = System.currentTimeMillis() - startTime;

            log.info("{} {} → {} ({}ms)",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration);

            MDC.clear();
        }
    }
}