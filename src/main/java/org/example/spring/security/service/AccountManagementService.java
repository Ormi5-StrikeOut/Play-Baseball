package org.example.spring.security.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.domain.member.Member;
import org.example.spring.exception.ResourceNotFoundException;
import org.example.spring.repository.MemberRepository;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountManagementService {
    private final MemberRepository memberRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final TokenBlacklistService tokenBlacklistService;
    private final CookieService cookieService;

    public AccountManagementService(MemberRepository memberRepository,
        JwtTokenValidator jwtTokenValidator,
        TokenBlacklistService tokenBlacklistService,
        CookieService cookieService) {
        this.memberRepository = memberRepository;
        this.jwtTokenValidator = jwtTokenValidator;
        this.tokenBlacklistService = tokenBlacklistService;
        this.cookieService = cookieService;
    }

    /**
     * 계정을 비활성화하고 관련 토큰을 무효화합니다.
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     */
    public void deactivateAccount(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtTokenValidator.extractTokenFromHeader(request);
        String email = jwtTokenValidator.extractUsername(token);

        log.info("Deactivating account for user: {}", email);

        // 1. 사용자 계정 비활성화
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));
        member.updateDeletedAt();
        memberRepository.save(member);

        // 2. 현재 사용 중인 토큰 무효화
        Date tokenExpiration = jwtTokenValidator.getTokenExpiration(token);
        tokenBlacklistService.addToBlacklist(token, tokenExpiration);

        // 3. 리프레시 토큰 제거
        cookieService.removeRefreshTokenCookie(response);

        log.info("Account deactivated successfully for user: {}", email);
    }
}
