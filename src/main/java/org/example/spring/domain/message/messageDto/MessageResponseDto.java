package org.example.spring.domain.message.messageDto;

import java.sql.Timestamp;

import org.example.spring.domain.member.dto.MemberMessageResponseDto;
import org.example.spring.domain.message.Message;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Builder
public class MessageResponseDto {
	private Long messageRoomId;

	private Long messageId;

	private MemberMessageResponseDto member;

	private String messageContent;

	private Timestamp createdAt;

	/**
	 *  Message 엔티티를 DTO로 변환하는 메서드
	 **/
	public static MessageResponseDto of(Message message) {
		return MessageResponseDto.builder()
			.messageRoomId(message.getMessageRoom().getId())
			.messageId(message.getId())
			.messageContent(message.getMessageContent())
			.member(MemberMessageResponseDto.fromMember(message.getMember()))
			.createdAt(message.getCreatedAt())
			.build();
	}

}
