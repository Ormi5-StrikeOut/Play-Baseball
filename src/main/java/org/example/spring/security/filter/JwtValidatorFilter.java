package org.example.spring.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.example.spring.security.jwt.CookieService;
import org.example.spring.security.service.JwtAuthenticationService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 들어오는 요청에 대해 JWT 토큰의 유효성을 검사하는 필터 클래스입니다.
 */
public class JwtValidatorFilter extends OncePerRequestFilter {

    private final CookieService cookieService;
    private final JwtAuthenticationService jwtAuthenticationService;

    public JwtValidatorFilter(CookieService cookieService, JwtAuthenticationService jwtAuthenticationService) {
        this.cookieService = cookieService;
        this.jwtAuthenticationService = jwtAuthenticationService;
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
        String accessToken = extractTokenFromHeader(request);
        String refreshToken = cookieService.extractTokenFromCookie(request, "refresh_token");
        try {
            jwtAuthenticationService.authenticateWithTokens(accessToken, refreshToken, response);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 요청 헤더에서 토큰을 추출합니다.
     *
     * @param request HTTP 요청
     * @return 추출된 토큰, 없으면 null
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 이 필터를 적용하지 않아야 하는 요청인지 결정합니다.
     *
     * @param request HTTP 요청
     * @return 필터를 적용하지 않아야 하면 true, 그렇지 않으면 false
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/api/members/join") || request.getServletPath().equals("/api/login");
    }
}
