package org.example.spring.security.jwt;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class JwtAuthenticationService {

    private final JwtTokenValidator jwtTokenValidator;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieService cookieService;

    public JwtAuthenticationService(JwtTokenValidator jwtTokenValidator, JwtTokenProvider jwtTokenProvider, CookieService cookieService) {
        this.jwtTokenValidator = jwtTokenValidator;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cookieService = cookieService;
    }

    public void authenticateWithTokens(String accessToken, String refreshToken, HttpServletResponse response) {
        if (accessToken != null && !jwtTokenValidator.isTokenExpired(accessToken)) {
            processToken(accessToken);
        } else if (refreshToken != null && !jwtTokenValidator.isTokenExpired(refreshToken)) {
            Authentication authentication = createAuthenticationFromToken(refreshToken);
            String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

            response.setHeader("Authorization", "Bearer " + newAccessToken);
            processToken(newAccessToken);
        } else {
            SecurityContextHolder.clearContext();
            throw new RuntimeException("Invalid or expired tokens");
        }
    }

    private void processToken(String token) {
        String username = jwtTokenValidator.extractUsername(token);
        String authoritiesString = jwtTokenValidator.validateToken(token).get("authorities", String.class);
        List<SimpleGrantedAuthority> authorities = Arrays.stream(authoritiesString.split(","))
            .map(SimpleGrantedAuthority::new)
            .toList();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private Authentication createAuthenticationFromToken(String token) {
        String username = jwtTokenValidator.extractUsername(token);
        String authoritiesString = jwtTokenValidator.validateToken(token).get("authorities", String.class);
        List<SimpleGrantedAuthority> authorities = Arrays.stream(authoritiesString.split(","))
            .map(SimpleGrantedAuthority::new)
            .toList();
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
}
