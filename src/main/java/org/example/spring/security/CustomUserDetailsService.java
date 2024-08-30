package org.example.spring.security;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.example.spring.domain.member.Member;
import org.example.spring.repository.MemberRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 사용자 인증 정보를 조회하는 사용자 정의 UserDetailsService 구현체 입니다.
 * Spring Security에서 사용자 인증을 위해 필요한 UserDetails 객체를 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    /**
     * 사용자 이메일을 이용하여 사용자의 인증 정보를 조회합니다.
     *
     * @param username 사용자 이메일
     * @return 사용자 인증 정보를 담은 UserDetails 객체
     * @throws UsernameNotFoundException 해당 사용자를 찾을 수 없는 경우 발생
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(username)
            .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾지 못했습니다: " + username));
        List<GrantedAuthority> authorities = Stream.of(member.getRole())
            .map(role -> new SimpleGrantedAuthority(role.name())).collect(
                Collectors.toList());
        return new User(member.getEmail(), member.getPassword(), authorities);
    }
}
