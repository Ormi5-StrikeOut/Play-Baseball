package org.example.spring.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.dto.LoginRequestDto;
import org.example.spring.domain.member.dto.LoginResponseDto;
import org.example.spring.exception.AccountDeletedException;
import org.example.spring.exception.AuthenticationFailedException;
import org.example.spring.exception.InvalidCredentialsException;
import org.example.spring.exception.ResourceNotFoundException;
import org.example.spring.repository.MemberRepository;
import org.example.spring.security.service.CookieService;
import org.example.spring.security.jwt.JwtTokenProvider;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.example.spring.security.service.TokenBlacklistService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 사용자 인증 서비스를 제공하는 클래스입니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenValidator jwtTokenValidator;
    private final TokenBlacklistService tokenBlacklistService;
    private final CookieService cookieService;
    private final MemberRepository memberRepository;

    /**
     * 사용자 로그인을 수행하고 JWT 토큰을 생성합니다.
     *
     * @param loginRequestDto 로그인 요청 데이터
     * @param response        HTTP 응답
     * @return 생성된 액세스 토큰
     */
    public LoginResponseDto login(LoginRequestDto loginRequestDto, HttpServletResponse response) {
        try {
            log.info("Attempting login for user: {}", loginRequestDto.email());

            if (!jwtTokenValidator.isAccountActive(loginRequestDto.email())) {
                log.warn("Login attempt for deleted account: {}", loginRequestDto.email());
                throw new AccountDeletedException("This account has been deleted.");
            }

            Authentication authentication = authenticateUser(loginRequestDto);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

            setTokensInResponse(response, accessToken, refreshToken);
            updateMemberLastLoginDate(loginRequestDto.email());

            log.info("User logged in successfully: {}", loginRequestDto.email());
            return createLoginResponse(authentication);
        } catch (AccountDeletedException e) {
            throw e;
        } catch (BadCredentialsException e) {
            log.warn("Login failed for user {}: Bad credentials", loginRequestDto.email());
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", loginRequestDto.email(), e.getMessage());
            throw new AuthenticationFailedException("Authentication failed. Please try again.");
        }
    }

    /**
     * 사용자 로그아웃을 수행합니다.
     *
     * @param request  HTTP 요청
     * @param response HTTP 응답
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String token = jwtTokenValidator.extractTokenFromHeader(request);
        if (token != null) {
            Date tokenExpiration = jwtTokenValidator.getTokenExpiration(token);
            tokenBlacklistService.addToBlacklist(token, tokenExpiration);
            log.debug("Token added to blacklist: {}", token);
        } else {
            log.warn("No token found in request during logout");
        }
        cookieService.removeRefreshTokenCookie(response);

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");

        log.info("User logged out successfully");
    }


    private Authentication authenticateUser(LoginRequestDto loginRequestDto) {
        return authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequestDto.email(), loginRequestDto.password())
        );
    }

    private void setTokensInResponse(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setHeader("Authorization", "Bearer " + accessToken);
        cookieService.addRefreshTokenCookie(response, refreshToken);
    }

    private void updateMemberLastLoginDate(String email) {
        Member member = memberRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Member", "email", email));
        member.updateLastLoginDate();
        memberRepository.save(member);
        log.debug("Updated last login date for user: {}", email);
    }

    private LoginResponseDto createLoginResponse(Authentication authentication) {
        return LoginResponseDto.toDto(authentication);
    }
}
