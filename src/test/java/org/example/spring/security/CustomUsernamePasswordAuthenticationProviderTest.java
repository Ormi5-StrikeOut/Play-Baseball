package org.example.spring.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

class CustomUsernamePasswordAuthenticationProviderTest {

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private CustomUsernamePasswordAuthenticationProvider authProvider;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        authProvider = new CustomUsernamePasswordAuthenticationProvider(userDetailsService, passwordEncoder);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("유효한 인증 정보로 인증 성공 확인")
    void authenticate_ValidCredentials_ReturnsAuthenticationToken() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        UserDetails userDetails = new User(email, password, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, userDetails.getPassword())).thenReturn(true);

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);

        // Act
        Authentication result = authProvider.authenticate(authentication);

        // Assert
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(email, result.getName());
    }


    @Test
    @DisplayName("잘못된 비밀번호로 인증 실패 시 예외 발생 확인")
    void authenticate_InvalidPassword_ThrowsBadCredentialsException() {
        // Arrange
        String email = "test@example.com";
        String password = "wrongpassword";
        UserDetails userDetails = new User(email, "correctpassword", Collections.emptyList());

        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(passwordEncoder.matches(password, userDetails.getPassword())).thenReturn(false);

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authProvider.authenticate(authentication));
    }

    @Test
    @DisplayName("UsernamePasswordAuthenticationToken 지원 여부 확인")
    void supports_UsernamePasswordAuthenticationToken_ReturnsTrue() {
        assertTrue(authProvider.supports(UsernamePasswordAuthenticationToken.class));
    }

}