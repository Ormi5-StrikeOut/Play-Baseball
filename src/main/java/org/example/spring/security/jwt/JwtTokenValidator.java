package org.example.spring.security.jwt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰의 유효성을 검사하고 정보를 추출하는 클래스입니다.
 */
@Component
@Slf4j
public class JwtTokenValidator {

    private final JwtUtils jwtUtils;
    private final Cache<String, Date> tokenBlacklist;

    public JwtTokenValidator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
        this.tokenBlacklist = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(1)) // 토큰을 블랙리스트에 추가한 후 1일 후에 자동으로 제거
            .maximumSize(10_000)
            .build();
    }

    /**
     * JWT 토큰을 검증하고 그 내용(claims)을 반환합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰에 포함된 claims
     * @throws JwtException 토큰이 유효하지 않은 경우
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(jwtUtils.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * JWT 토큰에서 사용자 이름을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에 포함된 사용자 이름
     */
    public String extractUsername(String token) {
        return validateToken(token).getSubject();
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     *
     * @param token 확인할 토큰
     * @return 블랙리스트에 있으면 true, 그렇지 않으면 false
     */
    public boolean isTokenBlacklisted(String token) {
        Date expirationDate = tokenBlacklist.getIfPresent(token);
        log.debug("Checking if token is blacklisted. Expiration date: {}", expirationDate);
        if (expirationDate != null) {
            log.debug("Token is blacklisted.");
            return true;
        }
        log.debug("Token is not in the blacklist.");
        return false;
    }

    /**
     * 토큰을 블랙리스트에 추가합니다.
     *
     * @param token 블랙리스트에 추가할 토큰
     */
    public void addToBlacklist(String token) {
        try {
            Claims claims = validateToken(token);
            Date expirationDate = claims.getExpiration();
            if (expirationDate.after(new Date())) {
                tokenBlacklist.put(token, expirationDate);
                log.info("Token added to blacklist. Expires at: {}", expirationDate);
            } else {
                log.warn("Attempted to blacklist an already expired token");
            }
        } catch (JwtException e) {
            log.warn("Attempted to blacklist an invalid token: {}", e.getMessage());
        }
    }

    /**
     * JWT 토큰이 유효한지 검사합니다. 토큰이 블랙리스트에 없고 만료되지 않은 경우에만 유효합니다.
     *
     * @param token 확인할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean isTokenValid(String token) {
        log.debug("Validating token: {}", token);

        // 블랙리스트 확인
        if (isTokenBlacklisted(token)) {
            log.debug("Token is blacklisted");
            return false;
        }

        try {
            Claims claims = validateToken(token);

            // 만료일 확인
            Date expirationDate = claims.getExpiration();
            if (expirationDate.before(new Date())) {
                log.debug("Token has expired");
                return false;
            }

            log.debug("Token validation result: valid");
            return true;
        } catch (JwtException e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
