package org.example.spring.security.jwt;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT 관련 유틸리티 기능을 제공하는 클래스입니다.
 * JWT 비밀키, 만료 시간 등의 설정을 관리합니다.
 */
@Slf4j
@Component
public class JwtUtils {

    /**
     * JWT 서명에 사용되는 비밀키입니다.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * 액세스 토큰의 만료 시간입니다.
     */
    @Getter
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 리프레시 토큰의 만료 시간입니다.
     */
    @Getter
    @Value("${jwt.refresh-token.expiration}")
    private Long refreshExpiration;

    /**
     * JWT 토큰 서명을 위한 키를 생성합니다.
     *
     * @return JWT 서명에 사용되는 Key 객체
     */
    public Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    /**
     * 요청 헤더에서 토큰을 추출합니다.
     *
     * @param request HTTP 요청
     * @return 추출된 토큰, 없으면 null
     */
    public String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("Extracted bearer token: {}", bearerToken);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            log.debug("Extracted JWT token: {}", token);
            return token;
        }
        log.debug("No valid JWT token found in request headers");
        return null;
    }

}
