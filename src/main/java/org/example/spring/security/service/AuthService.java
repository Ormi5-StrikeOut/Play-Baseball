package org.example.spring.security.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.spring.domain.member.dto.LoginRequestDto;
import org.example.spring.domain.member.dto.LoginResponseDto;
import org.example.spring.security.handler.CustomLogoutSuccessHandler;
import org.example.spring.security.jwt.CookieService;
import org.example.spring.security.jwt.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 사용자 인증 서비스를 제공하는 클래스입니다.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieService cookieService;
    private final CustomLogoutSuccessHandler customLogoutSuccessHandler;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, CookieService cookieService,
        CustomLogoutSuccessHandler customLogoutSuccessHandler) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieService = cookieService;
        this.customLogoutSuccessHandler = customLogoutSuccessHandler;
    }

    /**
     * 사용자 로그인을 수행하고 JWT 토큰을 생성합니다.
     *
     * @param loginRequestDto 로그인 요청 데이터
     * @param response HTTP 응답
     * @return 생성된 액세스 토큰
     */
    public LoginResponseDto login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        Authentication authentication = UsernamePasswordAuthenticationToken.unauthenticated(loginRequestDto.email(), loginRequestDto.password());
        Authentication authenticate = authenticationManager.authenticate(authentication);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.generateAccessToken(authenticate);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authenticate);

        response.setHeader("Authorization", "Bearer " + accessToken);
        cookieService.addRefreshTokenCookie(response, refreshToken);

        return LoginResponseDto.builder()
            .email(authenticate.getName())
            .roles(authenticate.getAuthorities().toString())
            .build();
    }

    /**
     * 사용자 로그아웃을 수행합니다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        customLogoutSuccessHandler.onLogoutSuccess(request, response, authentication);
    }

}
