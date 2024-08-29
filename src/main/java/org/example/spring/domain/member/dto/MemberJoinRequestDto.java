package org.example.spring.domain.member.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.example.spring.constants.Gender;
import org.example.spring.domain.member.Member;

/**
 * DTO for {@link Member}
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MemberJoinRequestDto {

    String email;
    String password;
    String nickname;
    String name;
    String phoneNumber;
    Gender gender;

}