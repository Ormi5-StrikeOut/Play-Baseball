package org.example.spring.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 사용자 정의 UsernamePasswordAuthenticationProvider 구현체 입니다.
 * Spring Security에서 사용자 인증 과정을 처리합니다.
 */
@Component
@RequiredArgsConstructor
public class CustomUsernamePasswordAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자가 제공한 로그인 정보를 이용하여 인증을 수행합니다.
     * @param authentication 사용자가 제공한 인증 토큰
     * @return 인증 성공 시 새로운 인증 토큰을 반환합니다.
     * @throws AuthenticationException 인증 실패 시 예외를 던집니다.
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (passwordEncoder.matches(password, userDetails.getPassword())) {
            return new UsernamePasswordAuthenticationToken(email, password, userDetails.getAuthorities());
        } else {
            throw new BadCredentialsException("잘못된 패스워드 입니다!");
        }
    }

    /**
     * 이 AuthenticationProvider가 지원하는 인증 토큰 타입을 반환합니다.
     *
     * @param authentication 인증 토큰 타입
     * @return 지원하는 경우 true, 지원하지 않는 경우 false
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
