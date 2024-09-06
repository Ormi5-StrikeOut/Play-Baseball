package org.example.spring.security.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.exception.InvalidTokenException;
import org.example.spring.security.jwt.CookieService;
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
    private final CookieService cookieService;
    private final AuthUtils authUtils;

    public JwtAuthenticationService(JwtTokenValidator jwtTokenValidator, JwtTokenProvider jwtTokenProvider, CookieService cookieService, AuthUtils authUtils) {
        this.jwtTokenValidator = jwtTokenValidator;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieService = cookieService;
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
        if (accessToken != null && jwtTokenValidator.validateToken(accessToken)) {
            log.debug("Authenticating with access token");
            return processToken(accessToken, request);
        } else if (refreshToken != null) {
            if (jwtTokenValidator.validateToken(refreshToken)) {
                log.debug("Access token invalid or missing, attempting to use refresh token");
                return refreshAccessToken(refreshToken, request, response);
            } else {
                log.warn("Refresh token is invalid or expired");
                handleExpiredRefreshToken(response);
                return null;
            }
        } else {
            log.warn("Both access token and refresh token are missing");
            handleInvalidTokens(response);
            return null;
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
    private Authentication refreshAccessToken(String refreshToken, HttpServletRequest request, HttpServletResponse response) {
        UserDetails userDetails = jwtTokenValidator.getUserDetails(refreshToken);
        Authentication authentication = createAuthenticationFromUserDetails(userDetails);
        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        response.setHeader("Authorization", "Bearer " + newAccessToken);
        Authentication newAuthentication = processToken(newAccessToken, request);
        log.debug("Access token refreshed for user: {}", userDetails.getUsername());
        return newAuthentication;
    }

    /**
     * 만료된 리프레시 토큰을 처리합니다.
     */
    private void handleExpiredRefreshToken(HttpServletResponse response) {
        cookieService.removeRefreshTokenCookie(response);
        throw new InvalidTokenException("Refresh token expired. Please log in again.");
    }

    /**
     * 유효하지 않은 토큰을 처리합니다.
     *
     * @throws InvalidTokenException 항상 발생
     */
    private void handleInvalidTokens(HttpServletResponse response) {
        cookieService.removeRefreshTokenCookie(response);
        throw new InvalidTokenException("Invalid or expired tokens. Please log in again.");
    }

    /**
     * UserDetails 로부터  UsernamePasswordAuthenticationToken 객체를 생성합니다.
     *
     * @return 생성된 UsernamePasswordAuthenticationToken 객체
     */
    private Authentication createAuthenticationFromUserDetails(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
