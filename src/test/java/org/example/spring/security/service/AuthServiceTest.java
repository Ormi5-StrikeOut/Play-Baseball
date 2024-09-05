package org.example.spring.security.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.spring.exception.InvalidTokenException;
import org.example.spring.security.jwt.CookieService;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtTokenValidator jwtTokenValidator;
    @Mock
    private CookieService cookieService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(null, null, jwtTokenValidator, cookieService, null);
    }

    @Test
    void testLogoutSuccess() {
        // Given
        String validToken = "valid_token";
        when(jwtTokenValidator.extractTokenFromHeader(request)).thenReturn(validToken);
        when(jwtTokenValidator.validateToken(validToken)).thenReturn(true);

        // When
        assertDoesNotThrow(() -> authService.logout(request, response));

        // Then
        verify(jwtTokenValidator).addToBlacklist(validToken);
        verify(cookieService).removeRefreshTokenCookie(response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void testLogoutWithInvalidToken() {
        // Given
        String invalidToken = "invalid_token";
        when(jwtTokenValidator.extractTokenFromHeader(request)).thenReturn(invalidToken);
        when(jwtTokenValidator.validateToken(invalidToken)).thenReturn(false);

        // When & Then
        assertThrows(InvalidTokenException.class, () -> authService.logout(request, response));

        verify(jwtTokenValidator, never()).addToBlacklist(anyString());
        verify(cookieService, never()).removeRefreshTokenCookie(any());
    }

    @Test
    void testLogoutWithNullToken() {
        // Given
        when(jwtTokenValidator.extractTokenFromHeader(request)).thenReturn(null);

        // When & Then
        assertThrows(InvalidTokenException.class, () -> authService.logout(request, response));

        verify(jwtTokenValidator, never()).addToBlacklist(anyString());
        verify(cookieService, never()).removeRefreshTokenCookie(any());
    }
}