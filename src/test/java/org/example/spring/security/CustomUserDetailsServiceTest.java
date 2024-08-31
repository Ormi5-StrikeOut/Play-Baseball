package org.example.spring.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.example.spring.constants.Gender;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class CustomUserDetailsServiceTest {

    @Mock
    private MemberRepository memberRepository;

    private CustomUserDetailsService userDetailsService;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        userDetailsService = new CustomUserDetailsService(memberRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }


    @Test
    @DisplayName("존재하는 사용자 정보 로드 확인")
    void loadUserByUsername_ExistingUser_ReturnsUserDetails() {
        // Arrange
        Member member = Member.builder()
            .id(1L)
            .name("testMan")
            .email("test@example.com")
            .nickname("testNickname")
            .password("encodedPassword")
            .phoneNumber("000-0000-0000")
            .gender(Gender.MALE)
            .role(MemberRole.USER)
            .build();

        when(memberRepository.findByEmail("test@example.com")).thenReturn(Optional.of(member));

        // Act
        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertTrue(result.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("USER")));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 예외 발생 확인")
    void loadUserByUsername_NonExistingUser_ThrowsUsernameNotFoundException() {
        // Arrange
        String email = "nonexistent@example.com";
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(email));
    }
}