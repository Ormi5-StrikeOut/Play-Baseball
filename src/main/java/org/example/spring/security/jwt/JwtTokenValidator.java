package org.example.spring.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.domain.member.Member;
import org.example.spring.exception.AccountDeletedException;
import org.example.spring.exception.InvalidTokenException;
import org.example.spring.exception.ResourceNotFoundException;
import org.example.spring.repository.MemberRepository;
import org.example.spring.security.service.TokenBlacklistService;
import org.example.spring.security.utils.JwtUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JWT 토큰의 유효성을 검사하고 관련 정보를 추출하는 클래스입니다. 이 클래스는 토큰의 검증, 사용자 정보 추출, 블랙리스트 관리 등의 기능을 제공합니다.
 */
@Component
@Slf4j
public class JwtTokenValidator {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final MemberRepository memberRepository;


    public JwtTokenValidator(JwtUtils jwtUtils, UserDetailsService userDetailsService, TokenBlacklistService tokenBlacklistService,
        MemberRepository memberRepository) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.memberRepository = memberRepository;
    }

    /**
     * 계정이 활성 상태인지 확인합니다.
     *
     * @param email 확인할 사용자의 이메일
     * @return 계정이 활성 상태이면 true, 그렇지 않으면 false
     */
    public boolean isAccountActive(String email) {
        return memberRepository.findByEmail(email)
            .map(member -> member.getDeletedAt() == null)
            .orElse(false);
    }

    /**
     * JWT 토큰의 유효성을 검증합니다. 토큰이 블랙리스트에 없고, 만료되지 않았으며, 사용자 정보와 일치하는지 확인합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        if (tokenBlacklistService.isTokenBlacklisted(token)) {
            log.debug("Token is blacklisted");
            return false;
        }

        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.debug("Token has expired");
            throw e;
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 액세스토큰과 리프레시토큰을 비교합니다
     *
     * @param accessToken  검증할 JWT access token
     * @param refreshToken 검증할 JWT refresh token
     * @return 유효하면 ture, 그렇지 않으면 false
     */
    public boolean validateAccessAndRefreshTokenConsistency(String accessToken, String refreshToken) {
        try {
            Claims accessClaims = extractAllClaims(accessToken);
            Claims refreshClaims = extractAllClaims(refreshToken);

            // 1. 사용자 이름(subject) 일치 확인
            if (!accessClaims.getSubject().equals(refreshClaims.getSubject())) {
                log.warn("Username mismatch between access token and refresh token");
                return false;
            }

            // 2. 토큰 발행 시간(iat) 비교
            Date accessIssuedAt = accessClaims.getIssuedAt();
            Date refreshIssuedAt = refreshClaims.getIssuedAt();
            if (accessIssuedAt.before(refreshIssuedAt)) {
                log.warn("Access token was issued before refresh token");
                return false;
            }

            // 3. 액세스 토큰 만료 확인
            if (accessClaims.getExpiration().before(new Date())) {
                throw new ExpiredJwtException(null, accessClaims, "Access token has expired");
            }

            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Access token has expired");
            throw e;
        } catch (Exception e) {
            log.error("Error validating token consistency: {}", e.getMessage());
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
     * JWT 토큰에서 토큰 만료일을 추출합니다.
     *
     * @param token JWT 토큰
     * @return 토큰 만료일
     */
    public Date getTokenExpiration(String token) {
        try {
            return extractAllClaims(token).getExpiration();
        } catch (ExpiredJwtException e) {
            if (e.getClaims() != null) {
                Date expiration = e.getClaims().getExpiration();
                if (expiration != null) {
                    return expiration;
                }
            }
            log.warn("No valid expiration date found in expired token");
            return null;
        } catch (Exception e) {
            log.error("Error extracting expiration date from token: {}", e.getMessage());
            return null;
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
    public Member getMemberFromToken(String token) {
        String email = extractUsername(token);
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> {
                log.error("Member not found for email: {}", email);
                return new ResourceNotFoundException("Member", "email", email);
            });

        if (member.getDeletedAt() != null) {
            log.warn("Attempt to access with deleted account: {}", email);
            throw new AccountDeletedException("This account has been deleted");
        }

        return member;
    }

    /**
     * 토큰의 유효성을 검사하고 멤버를 반환합니다.
     *
     * @param token JWT 토큰
     * @return 검증된 토큰에 해당하는 Member 객체
     * @throws InvalidTokenException     토큰이 유효하지 않은 경우
     * @throws ResourceNotFoundException 멤버를 찾을 수 없는 경우
     */
    public Member validateTokenAndGetMember(String token) {
        if (validateToken(token)) {
            return getMemberFromToken(token);
        }
        throw new InvalidTokenException("Invalid or expired JWT token");
    }
}
