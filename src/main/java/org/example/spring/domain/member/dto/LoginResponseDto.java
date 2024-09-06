package org.example.spring.domain.member.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.security.core.Authentication;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class LoginResponseDto {

    private String email;
    private String role;

    public static LoginResponseDto toDto(Authentication Authentication) {
        return LoginResponseDto.builder()
            .email(Authentication.getName())
            .role(Authentication.getAuthorities().toString())
            .build();
    }
}
