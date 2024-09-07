package org.example.spring.domain.member.dto;

import jakarta.validation.constraints.NotNull;
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
public class MemberRoleModifyRequestDto {

    @NotNull(message = "권한 정보는 필수입니다.")
    private MemberRole role;
}
