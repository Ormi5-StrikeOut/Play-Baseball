package org.example.spring.security.jwt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.domain.member.Member;
import org.example.spring.exception.InvalidTokenException;
import org.example.spring.exception.ResourceNotFoundException;
import org.example.spring.repository.MemberRepository;
import org.example.spring.security.utils.JwtUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT 토큰의 유효성을 검사하고 관련 정보를 추출하는 클래스입니다.
 * 이 클래스는 토큰의 검증, 사용자 정보 추출, 블랙리스트 관리 등의 기능을 제공합니다.
 */
@Component
@Slf4j
public class JwtTokenValidator {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final MemberRepository memberRepository;
    private final Cache<String, Date> tokenBlacklist;

    public JwtTokenValidator(JwtUtils jwtUtils, UserDetailsService userDetailsService, MemberRepository memberRepository) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.memberRepository = memberRepository;
        this.tokenBlacklist = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofDays(1)) // 토큰을 블랙리스트에 추가한 후 1일 후에 자동으로 제거
            .maximumSize(10_000)
            .build();
    }

    /**
     * JWT 토큰의 유효성을 검증합니다.
     * 토큰이 블랙리스트에 없고, 만료되지 않았으며, 사용자 정보와 일치하는지 확인합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        if (isTokenBlacklisted(token)) {
            log.debug("Token is blacklisted");
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);

            String username = claims.getSubject();
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            boolean isValid = username.equals(userDetails.getUsername());
            log.debug("Token validation result for user {}: {}", username, isValid);
            return isValid;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 주어진 토큰에 해당하는 사용자 정보를 반환합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에 해당하는 UserDetails 객체
     * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우
     */
    public UserDetails getUserDetails(String token) {
        String username = extractUsername(token);
        return userDetailsService.loadUserByUsername(username);
    }

    /**
     * JWT 토큰에서 모든 클레임을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에 포함된 모든 클레임
     * @throws InvalidTokenException 토큰이 유효하지 않은 경우
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(jwtUtils.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (JwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new InvalidTokenException("Invalid token");
        }
    }

    /**
     * JWT 토큰에서 사용자 이름(email)을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에 포함된 사용자 이름
     */
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
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
        return expirationDate != null;
    }

    /**
     * 토큰을 블랙리스트에 추가합니다.
     *
     * @param token 블랙리스트에 추가할 토큰
     */
    public void addToBlacklist(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expirationDate = claims.getExpiration();
            if (expirationDate.after(new Date())) {
                tokenBlacklist.put(token, expirationDate);
                log.info("Token added to blacklist. Expires at: {}", expirationDate);
            } else {
                log.warn("Attempted to blacklist an already expired token");
            }
        } catch (InvalidTokenException e) {
            log.warn("Attempted to blacklist an invalid token: {}", e.getMessage());
        }
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

    /**
     * JWT 토큰에서 멤버 정보를 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰에 해당하는 Member 객체
     * @throws ResourceNotFoundException 멤버를 찾을 수 없는 경우
     */
    private Member getMemberFromToken(String token) {
        String email = extractUsername(token);
        return memberRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("Member not found for email: {}", email);
                return new ResourceNotFoundException("Member", "email", email);
            });
    }

    /**
     * 토큰의 유효성을 검사하고 멤버를 반환합니다.
     *
     * @param token JWT 토큰
     * @return 검증된 토큰에 해당하는 Member 객체
     * @throws InvalidTokenException 토큰이 유효하지 않은 경우
     * @throws ResourceNotFoundException 멤버를 찾을 수 없는 경우
     */
    public Member validateTokenAndGetMember(String token) {
        if (validateToken(token)) {
            return getMemberFromToken(token);
        }
        throw new InvalidTokenException("Invalid or expired JWT token");
    }
}
