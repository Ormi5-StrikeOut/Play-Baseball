package org.example.spring.security.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.example.spring.domain.member.dto.LoginRequestDto;
import org.example.spring.domain.member.dto.LoginResponseDto;
import org.example.spring.exception.InvalidTokenException;
import org.example.spring.security.jwt.CookieService;
import org.example.spring.security.jwt.JwtTokenProvider;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.example.spring.security.jwt.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private JwtTokenValidator jwtTokenValidator;

    @Mock
    private CookieService cookieService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, jwtTokenProvider, jwtTokenValidator, cookieService);
    }

    @Test
    void testLogin() {
        // Given
        LoginRequestDto loginRequestDto = new LoginRequestDto("test@example.com", "password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = mock(Authentication.class);

        when(authentication.getName()).thenReturn("test@example.com");

        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
            .when(authentication).getAuthorities();

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateAccessToken(authentication)).thenReturn("access_token");
        when(jwtTokenProvider.generateRefreshToken(authentication)).thenReturn("refresh_token");

        // When
        LoginResponseDto result = authService.login(loginRequestDto, response);

        // Then
        assertEquals("test@example.com", result.getEmail());
        assertTrue(result.getRoles().contains("ROLE_USER"));
        assertEquals("Bearer access_token", response.getHeader("Authorization"));
        verify(cookieService).addRefreshTokenCookie(response, "refresh_token");
    }

    @Test
    void testLogoutSuccess() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtTokenValidator.extractTokenFromHeader(request)).thenReturn("valid_token");
        when(jwtTokenValidator.isTokenBlacklisted("valid_token")).thenReturn(false);

        // When
        authService.logout(request, response);

        // Then
        verify(jwtTokenValidator).addToBlacklist("valid_token");
        verify(cookieService).removeRefreshTokenCookie(response);
    }

    @Test
    void testLogoutWithNoToken() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtTokenValidator.extractTokenFromHeader(request)).thenReturn(null);

        // When & Then
        assertThrows(InvalidTokenException.class, () -> authService.logout(request, response));
    }

    @Test
    void testLogoutWithBlacklistedToken() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtTokenValidator.extractTokenFromHeader(request)).thenReturn("blacklisted_token");
        when(jwtTokenValidator.isTokenBlacklisted("blacklisted_token")).thenReturn(true);

        // When & Then
        assertThrows(InvalidTokenException.class, () -> authService.logout(request, response));
    }
}