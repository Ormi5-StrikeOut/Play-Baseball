package org.example.spring.domain.member.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.spring.domain.member.Member;

/**
 * DTO for {@link Member}
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MemberResponseDto {

    private String email;
    private String nickname;
    private String name;

    public static MemberResponseDto toDto(Member member) {
        return MemberResponseDto.builder()
            .email(member.getEmail())
            .nickname(member.getNickname())
            .name(member.getName())
            .build();
    }
}
