package org.example.spring.security.jwt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
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
            .expireAfter(new Expiry<String, Date>() {
                @Override
                public long expireAfterCreate(String key, Date value, long currentTime) {
                    return value.getTime() - currentTime;
                }
                @Override
                public long expireAfterUpdate(String key, Date value, long currentTime, long currentDuration) {
                    return value.getTime() - currentTime;
                }
                @Override
                public long expireAfterRead(String key, Date value, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
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
        if (expirationDate != null) {
            if (expirationDate.before(new Date())) {
                tokenBlacklist.invalidate(token);
                return false;
            }
            return true;
        }
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
     * JWT 토큰의 유효성을 검사합니다. 토큰이 만료되지 않았고 블랙리스트에 없는 경우에만 유효합니다.
     *
     * @param token 확인할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean isTokenValid(String token) {
        if (isTokenBlacklisted(token)) {
            return false;
        }
        try {
            Claims claims = validateToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }
}
