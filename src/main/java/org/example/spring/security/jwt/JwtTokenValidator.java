package org.example.spring.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Date;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰의 유효성을 검사하고 정보를 추출하는 클래스입니다.
 */
@Component
public class JwtTokenValidator {

    private final JwtUtils jwtUtils;

    public JwtTokenValidator(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    /**
     * JWT 토큰을 검증하고 그 내용(claims)을 반환합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰에 포함된 claims
     */
    public Claims validateToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(jwtUtils.getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
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
     * JWT 토큰의 만료 여부를 확인합니다.
     *
     * @param token 확인할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean isTokenValid(String token) {
        return !validateToken(token).getExpiration().before(new Date());
    }


}
