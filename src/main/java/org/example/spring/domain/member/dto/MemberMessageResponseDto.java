package org.example.spring.domain.member.dto;

import org.example.spring.domain.member.Member;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberMessageResponseDto {
	private Long id;
	private String nickname;

	public static MemberMessageResponseDto fromMember(Member member) {
		return MemberMessageResponseDto.builder()
			.id(member.getId())
			.nickname(member.getNickname())
			.build();
	}
}
