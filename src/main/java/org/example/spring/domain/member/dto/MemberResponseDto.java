package org.example.spring.domain.member.dto;

import java.sql.Timestamp;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;

/**
 * DTO for {@link Member}
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MemberResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private MemberRole role;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private Timestamp lastLoginDate;
    private Timestamp deletedAt;

    public static MemberResponseDto toDto(Member member) {
        return MemberResponseDto.builder()
            .id(member.getId())
            .email(member.getEmail())
            .nickname(member.getNickname())
            .role(member.getRole())
            .createdAt(member.getCreatedAt())
            .updatedAt(member.getUpdatedAt())
            .lastLoginDate(member.getLastLoginDate())
            .deletedAt(member.getDeletedAt())
            .build();
    }
}
