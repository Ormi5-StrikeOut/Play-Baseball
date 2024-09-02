package org.example.spring.service;

import jakarta.servlet.http.HttpServletResponse;
import org.example.spring.domain.member.dto.LoginRequestDto;
import org.example.spring.security.jwt.CookieService;
import org.example.spring.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * 사용자 인증 서비스를 제공하는 클래스입니다.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieService cookieService;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, CookieService cookieService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieService = cookieService;
    }

    /**
     * 사용자 로그인을 수행하고 JWT 토큰을 생성합니다.
     *
     * @param loginRequestDto 로그인 요청 데이터
     * @param response HTTP 응답
     * @return 생성된 액세스 토큰
     */
    public String login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(loginRequestDto.email(), loginRequestDto.password());
        Authentication authenticate = authenticationManager.authenticate(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authenticate);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authenticate);

        cookieService.addRefreshTokenCookie(response, refreshToken);
        return accessToken;
    }

}
