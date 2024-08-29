package org.example.spring.domain.member.dto;

import lombok.Data;
import org.example.spring.constants.Gender;
import org.example.spring.domain.member.Member;

/**
 * DTO for {@link Member}
 */
@Data
public class MemberJoinRequestDto {

    String email;
    String password;
    String nickname;
    String name;
    String phoneNumber;
    Gender gender;

}