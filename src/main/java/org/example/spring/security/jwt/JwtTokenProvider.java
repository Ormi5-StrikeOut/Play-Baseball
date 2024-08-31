package org.example.spring.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private final JwtUtils jwtUtils;

    public JwtTokenProvider(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, jwtUtils.getExpiration());
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, jwtUtils.getRefreshExpiration());
    }

    private  String generateToken(Authentication authentication, Long expirationTime) {
        return Jwts.builder()
            .setIssuer("play_baseball")
            .setSubject(authentication.getName())
            .claim("authorities", getAuthorities(authentication))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
            .setId(UUID.randomUUID().toString())
            .signWith(jwtUtils.getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    private String getAuthorities(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
    }

    public long getAccessTokenExpiration() {
        return jwtUtils.getExpiration();
    }

    public long getRefreshTokenExpiration() {
        return jwtUtils.getRefreshExpiration();
    }
}
