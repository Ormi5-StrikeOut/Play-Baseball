package org.example.spring.domain.message.messageDto;

import lombok.*;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.message.Message;

import java.sql.Timestamp;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Builder
public class MessageResponseDto {
    private Long messageRoomId;

    private Long messageId;

    private Long memberId;

    private String messageContent;

    private Timestamp createAt;

    /**
     *  Message 엔티티를 DTO로 변환하는 메서드
    **/
    public static MessageResponseDto of(Message message) {
        return MessageResponseDto.builder()
                .messageRoomId(message.getMessageRoom().getId())
                .messageId(message.getId())
                .messageContent(message.getMessageContent())
                .memberId(message.getMember().getId())
                .createAt(message.getCreatedAt())
                .build();
    }

}
