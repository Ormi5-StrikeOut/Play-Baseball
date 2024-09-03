package org.example.spring.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.security.jwt.JwtUtils;
import org.example.spring.security.service.RateLimiterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiter;
    private final JwtUtils jwtUtils;

    public RateLimitFilter(RateLimiterService rateLimiter, JwtUtils jwtUtils) {
        this.rateLimiter = rateLimiter;
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String token = jwtUtils.extractTokenFromHeader(request);
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("Received request - hasToken: {}, IP: {}, User-Agent: {}", !token.isEmpty(), clientIp, userAgent);

        if (rateLimiter.tryConsume(token, clientIp, userAgent)) {
            log.info("Rate limit passed - hasToken: {}, IP: {}, User-Agent: {}", !token.isEmpty(), clientIp, userAgent);
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate limit exceeded - hasToken: {}, IP: {}, User-Agent: {}", !token.isEmpty(), clientIp, userAgent);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
            errorResponse.put("error", HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase());
            errorResponse.put("message", "Too many requests. Please try again later.");
            errorResponse.put("path", request.getRequestURI());

            response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
        }
    }

    private String getClientIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
