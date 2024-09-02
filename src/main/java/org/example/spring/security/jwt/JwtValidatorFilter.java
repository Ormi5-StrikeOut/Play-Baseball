package org.example.spring.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtValidatorFilter extends OncePerRequestFilter {

    private final CookieService cookieService;
    private final JwtAuthenticationService jwtAuthenticationService;

    public JwtValidatorFilter(CookieService cookieService, JwtAuthenticationService jwtAuthenticationService) {
        this.cookieService = cookieService;
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        String accessToken = extractTokenFromHeader(request);
        String refreshToken = cookieService.extractTokenFromCookie(request, "refresh_token");
        try {
            jwtAuthenticationService.authenticateWithTokens(accessToken, refreshToken, response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/api/members/join") || request.getServletPath().equals("/api/login");
    }
}
