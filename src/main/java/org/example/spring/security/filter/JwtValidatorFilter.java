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
        "/", "/api/auth/login", "/api/members/join", "/api/exchanges", "/api/reviews"
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
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외 발생 시
     * @throws IOException I/O 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String accessToken = jwtTokenValidator.extractTokenFromHeader(request);
        String refreshToken = cookieService.extractTokenFromCookie(request, "refresh_token");

        try {
            if (accessToken != null && jwtTokenValidator.isTokenBlacklisted(accessToken)) {
                log.debug("Blacklisted token used: {}", accessToken);
                throw new InvalidTokenException("This token has been blacklisted.");
            }

            jwtAuthenticationService.authenticateWithTokens(accessToken, refreshToken, request, response);

            filterChain.doFilter(request, response);
        } catch (InvalidTokenException e) {
            log.warn("Invalid token: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication Error");
        }
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
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
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith) ||
            path.startsWith("/api/members/verify/");
    }
}