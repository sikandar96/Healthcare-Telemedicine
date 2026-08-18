package com.health.care.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.health.care.dtos.HealthApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight node-local abuse protection. A distributed deployment should
 * move these counters to a shared store or enforce equivalent limits at the
 * gateway/API-management layer.
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    private static final long WINDOW_MILLIS = 60_000L;
    private static final int GENERAL_LIMIT = 120;
    private static final int AUTH_LIMIT = 10;
    private static final int PAYMENT_LIMIT = 20;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || isExempt(request.getRequestURI())) {
            chain.doFilter(request, response);
            return;
        }

        String category = category(request.getRequestURI());
        int limit = switch (category) {
            case "auth" -> AUTH_LIMIT;
            case "payment" -> PAYMENT_LIMIT;
            default -> GENERAL_LIMIT;
        };
        String key = request.getRemoteAddr() + ":" + category;
        Window window = windows.compute(key, (ignored, current) -> nextWindow(current));
        if (window.count.incrementAndGet() > limit) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Retry-After", "60");
            logger.warn("Rate limit exceeded: category={} remoteAddress={}", category, request.getRemoteAddr());
            response.getWriter().write(objectMapper.writeValueAsString(
                    HealthApiResponse.error("Too many requests. Please retry later.")));
            return;
        }
        chain.doFilter(request, response);
        cleanupExpiredWindows();
    }

    private Window nextWindow(Window current) {
        long now = Instant.now().toEpochMilli();
        return current == null || now - current.startedAt >= WINDOW_MILLIS ? new Window(now) : current;
    }

    private void cleanupExpiredWindows() {
        if (windows.size() < 2_000) return;
        long cutoff = Instant.now().toEpochMilli() - WINDOW_MILLIS;
        windows.entrySet().removeIf(entry -> entry.getValue().startedAt < cutoff);
    }

    private boolean isExempt(String uri) {
        return uri.startsWith("/actuator/health") || uri.startsWith("/swagger-ui") || uri.startsWith("/v3/api-docs");
    }

    private String category(String uri) {
        if (uri.startsWith("/api/auth/")) return "auth";
        if (uri.startsWith("/api/platform/payments") || uri.contains("medicine-orders")) return "payment";
        return "general";
    }

    private static final class Window {
        private final long startedAt;
        private final AtomicInteger count = new AtomicInteger();

        private Window(long startedAt) {
            this.startedAt = startedAt;
        }
    }
}

