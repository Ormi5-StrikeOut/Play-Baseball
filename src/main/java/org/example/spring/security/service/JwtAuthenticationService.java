package org.example.spring.security.service;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.security.jwt.JwtTokenProvider;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.example.spring.security.utils.AuthUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;

/**
 * JWT 토큰을 사용한 인증 프로세스를 처리하는 서비스 클래스입니다.
 */
@Slf4j
@Service
public class JwtAuthenticationService {

    private final JwtTokenValidator jwtTokenValidator;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthUtils authUtils;

    public JwtAuthenticationService(JwtTokenValidator jwtTokenValidator, JwtTokenProvider jwtTokenProvider, AuthUtils authUtils) {
        this.jwtTokenValidator = jwtTokenValidator;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authUtils = authUtils;
    }

    /**
     * 액세스 토큰과 리프레시 토큰을 사용하여 사용자를 인증합니다.
     *
     * @param accessToken 액세스 토큰
     * @param refreshToken 리프레시 토큰
     * @param response HTTP 응답
     * @throws RuntimeException 두 토큰 모두 유효하지 않거나 만료된 경우
     */
    public Authentication authenticateWithTokens(String accessToken, String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        try {
            if (accessToken != null && refreshToken != null &&
                jwtTokenValidator.validateAccessAndRefreshTokenConsistency(accessToken, refreshToken)) {
                return processToken(accessToken, request);
            } else {
                throw new ExpiredJwtException(null, null, "Access token is expired or invalid");
            }
        } catch (ExpiredJwtException e) {
            log.debug("Access token expired or invalid, throwing ExpiredJwtException");
            throw e;
        }
    }

    /**
     * JWT 토큰을 처리하고 인증 정보를 설정합니다.
     *
     * @param token 처리할 JWT 토큰
     */
    private Authentication processToken(String token, HttpServletRequest request) {

        log.debug("Processing token: {}", token);

        String ip = authUtils.getClientIpAddress(request);

        /*if (rateLimiterService.isIpChanged(userDetails.getUsername(), ip)) {
            String storedIp = rateLimiterService.getStoredIp(userDetails.getUsername());
            log.warn("IP address changed for user: {}. Old IP: {}, New IP: {}",
                userDetails.getUsername(), storedIp, ip);
            handleInvalidTokens();
            return;
        }*/
        UserDetails userDetails = jwtTokenValidator.getUserDetails(token);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authentication;
    }

    /**
     * 리프레시 토큰을 사용하여 새로운 액세스 토큰을 생성합니다.
     *
     * @param refreshToken 리프레시 토큰
     * @param request HTTP 요청
     * @param response HTTP 응답
     */
    public Authentication refreshAccessToken(String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        UserDetails userDetails = jwtTokenValidator.getUserDetails(refreshToken);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

        response.setHeader("Authorization", "Bearer " + newAccessToken);
        response.setHeader("Access-Control-Expose-Headers", "Authorization");

        return processToken(newAccessToken, request);
    }

}
