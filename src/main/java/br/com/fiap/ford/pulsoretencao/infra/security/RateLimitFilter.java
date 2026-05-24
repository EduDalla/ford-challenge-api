package br.com.fiap.ford.pulsoretencao.infra.security;

import br.com.fiap.ford.pulsoretencao.shared.api.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final int maxRequests;
    private final long windowMs;
    private final int authMaxRequests;
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, Deque<Long>> requestLog = new ConcurrentHashMap<>();

    public RateLimitFilter(
            @Value("${app.rate-limit.max-requests:60}") int maxRequests,
            @Value("${app.rate-limit.window-seconds:60}") int windowSeconds,
            @Value("${app.rate-limit.auth-max-requests:10}") int authMaxRequests,
            ObjectMapper objectMapper
    ) {
        this.maxRequests = maxRequests;
        this.windowMs = windowSeconds * 1000L;
        this.authMaxRequests = authMaxRequests;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getServletPath();
        return "OPTIONS".equalsIgnoreCase(method)
                || path.equals("/health")
                || path.equals("/healthCheck")
                || path.equals("/")
                || path.equals("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        boolean isAuthEndpoint = path.startsWith("/api/v1/auth/");
        String ip = resolveClientIp(request);
        String key = ip + (isAuthEndpoint ? ":auth" : "");
        int limit = isAuthEndpoint ? authMaxRequests : maxRequests;

        if (!isAllowed(key, limit)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse error = new ErrorResponse(
                    LocalDateTime.now(),
                    429,
                    "Too Many Requests",
                    "Limite de requisicoes excedido. Tente novamente em instantes.",
                    path,
                    null
            );
            objectMapper.writeValue(response.getWriter(), error);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAllowed(String key, int limit) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;
        Deque<Long> timestamps = requestLog.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
                timestamps.pollFirst();
            }
            if (timestamps.size() >= limit) {
                return false;
            }
            timestamps.addLast(now);
        }
        return true;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
