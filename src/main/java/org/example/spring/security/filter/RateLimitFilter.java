package org.example.spring.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.example.spring.security.service.RateLimiterService;
import org.example.spring.security.utils.AuthUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiter;
    private final JwtTokenValidator jwtTokenValidator;
    private final AuthUtils authUtils;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(RateLimiterService rateLimiter, JwtTokenValidator jwtTokenValidator, AuthUtils authUtils, ObjectMapper objectMapper) {
        this.rateLimiter = rateLimiter;
        this.jwtTokenValidator = jwtTokenValidator;
        this.authUtils = authUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String token = jwtTokenValidator.extractTokenFromHeader(request);
        String ip = authUtils.getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");

        if (rateLimiter.tryConsume(token, ip, userAgent)) {
            filterChain.doFilter(request, response);
        } else {
            createErrorResponse(response);
        }
    }

    private void createErrorResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorResponse = Map.of(
            "status", HttpStatus.TOO_MANY_REQUESTS.value(),
            "error", "Too Many Requests",
            "message", "Rate limit exceeded. Please try again later."
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}