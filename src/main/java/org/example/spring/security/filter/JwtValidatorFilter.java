package org.example.spring.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.exception.InvalidTokenException;
import org.example.spring.security.jwt.CookieService;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.example.spring.security.service.JwtAuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 들어오는 요청에 대해 JWT 토큰의 유효성을 검사하는 필터 클래스입니다.
 */
@Slf4j
@Component
public class JwtValidatorFilter extends OncePerRequestFilter {

    private final CookieService cookieService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final JwtTokenValidator jwtTokenValidator;

    private static final List<String> PUBLIC_PATHS = Arrays.asList(
        "/swagger-ui", "/v3/api-docs", "/webjars", "/api/auth/login", "/api/members/join"
    );

    public JwtValidatorFilter(CookieService cookieService, JwtAuthenticationService jwtAuthenticationService,
        JwtTokenValidator jwtTokenValidator) {
        this.cookieService = cookieService;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.jwtTokenValidator = jwtTokenValidator;
    }

    /**
     * 들어오는 요청을 필터링하여 JWT 토큰의 유효성을 검사하고 인증을 설정합니다.
     *
     * @param request     HTTP 요청
     * @param response    HTTP 응답
     * @param filterChain 필터 체인
     * @throws IOException I/O 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws IOException, ServletException {
        try {
            String requestURI = request.getRequestURI();
            log.debug("Processing request: {}", requestURI);
            String accessToken = jwtTokenValidator.extractTokenFromHeader(request);
            String refreshToken = cookieService.extractTokenFromCookie(request, "refresh_token");

            log.debug("Extracted tokens - Access: {}, Refresh: {}",
                accessToken != null ? "Present" : "Not present",
                refreshToken != null ? "Present" : "Not present");

            if (accessToken == null || refreshToken == null) {
                log.warn("Missing access token or refresh token for request: {}", requestURI);
                handleMissingTokens(response);
                return;
            }

            Authentication authentication = jwtAuthenticationService.authenticateWithTokens(accessToken, refreshToken, request, response);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Authentication successful for request: {}", requestURI);
            } else {
                log.warn("Authentication failed for request: {}", requestURI);
                handleAuthenticationFailure(response);
                return;
            }

            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            log.warn("Invalid token: {}", e.getMessage());
            handleInvalidTokens(response);
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            handleAuthenticationError(response, e);
        }
    }

    private void handleMissingTokens(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Missing access token or refresh token");
    }

    private void handleInvalidTokens(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Invalid or inconsistent tokens");
    }

    private void handleAuthenticationFailure(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Authentication failed");
    }

    private void handleAuthenticationError(HttpServletResponse response, Exception e) throws IOException {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write("An error occurred during authentication: " + e.getMessage());
    }

    /**
     * 이 필터를 적용하지 않아야 하는 요청인지 결정합니다.
     *
     * @param request HTTP 요청
     * @return 필터를 적용하지 않아야 하면 true, 그렇지 않으면 false
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        boolean shouldSkip = PUBLIC_PATHS.stream().anyMatch(path::startsWith) || path.startsWith("/api/members/verify/");
        log.debug("Should skip filter for path {}: {}", path, shouldSkip);
        return shouldSkip;
    }
}