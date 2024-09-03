package org.example.spring.security.service;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import org.example.spring.security.jwt.JwtTokenProvider;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * JWT 토큰을 사용한 인증 프로세스를 처리하는 서비스 클래스입니다.
 */
@Service
public class JwtAuthenticationService {

    private final JwtTokenValidator jwtTokenValidator;
    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationService(JwtTokenValidator jwtTokenValidator, JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenValidator = jwtTokenValidator;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 액세스 토큰과 리프레시 토큰을 사용하여 사용자를 인증합니다.
     *
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param response HTTP 응답
     * @throws RuntimeException 두 토큰 모두 유효하지 않거나 만료된 경우
     */
    public void authenticateWithTokens(String accessToken, String refreshToken, HttpServletResponse response) {
        if (accessToken != null && jwtTokenValidator.isTokenValid(accessToken)) {
            processToken(accessToken);
        } else if (refreshToken != null && jwtTokenValidator.isTokenValid(refreshToken)) {
            Authentication authentication = createAuthenticationFromToken(refreshToken);
            String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

            response.setHeader("Authorization", "Bearer " + newAccessToken);
            processToken(newAccessToken);
        } else {
            SecurityContextHolder.clearContext();
            throw new RuntimeException("Invalid or expired tokens");
        }
    }

    /**
     * JWT 토큰을 처리하고 인증 정보를 설정합니다.
     *
     * @param token 처리할 JWT 토큰
     */
    private void processToken(String token) {
        String username = jwtTokenValidator.extractUsername(token);
        String authoritiesString = jwtTokenValidator.validateToken(token).get("authorities", String.class);
        List<SimpleGrantedAuthority> authorities = Arrays.stream(authoritiesString.split(","))
            .map(SimpleGrantedAuthority::new)
            .toList();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    /**
     * JWT 토큰으로부터 Authentication 객체를 생성합니다.
     *
     * @param token 인증 정보를 추출할 JWT 토큰
     * @return 생성된 Authentication 객체
     */
    private Authentication createAuthenticationFromToken(String token) {
        String username = jwtTokenValidator.extractUsername(token);
        String authoritiesString = jwtTokenValidator.validateToken(token).get("authorities", String.class);
        List<SimpleGrantedAuthority> authorities = Arrays.stream(authoritiesString.split(","))
            .map(SimpleGrantedAuthority::new)
            .toList();
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
