package org.example.spring.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.security.utils.JwtUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰을 생성하는 클래스입니다.
 * 액세스 토큰과 리프레시 토큰을 생성합니다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final JwtUtils jwtUtils;

    public JwtTokenProvider(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * 주어진 인증 정보로 액세스 토큰을 생성합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return JWT 액세스 토큰
     */
    public String generateAccessToken(Authentication authentication) {
        log.info("Access token generated for user: {}", authentication.getName());
        return generateToken(authentication, jwtUtils.getExpiration());
    }

    /**
     * 주어진 인증 정보로 리프레시 토큰을 생성합니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return JWT 리프레시 토큰
     */
    public String generateRefreshToken(Authentication authentication) {
        log.info("Refresh token generated for user: {}", authentication.getName());
        return generateToken(authentication, jwtUtils.getRefreshExpiration());
    }

    /**
     * 인증 정보를 기반으로 토큰을 생성합니다.
     *
     * @param authentication 인증 정보
     * @param expirationTime 토큰 만료 시간
     * @return 생성된 JWT 토큰
     */
    private  String generateToken(Authentication authentication, Long expirationTime) {
        return Jwts.builder()
            .setIssuer("play_baseball")
            .setSubject(authentication.getName())
            .claim("authorities", getAuthorities(authentication))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
            .setId(UUID.randomUUID().toString())
            .signWith(jwtUtils.getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    private String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
    }

    public long getRefreshTokenExpiration() {
        return jwtUtils.getRefreshExpiration();
    }
}
