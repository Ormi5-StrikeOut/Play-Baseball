package org.example.spring.domain.message.messageDto;

import lombok.*;
import org.example.spring.domain.message.MessageRoom;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
@Getter
@Builder
public class MessageRoomResponseDto {

    private Long messageRoomId;

    private Timestamp createAt;

    private Timestamp lastMessageAt;

    private List<MessageResponseDto> messages;

    public static MessageRoomResponseDto of(MessageRoom messageRoom) {
        return MessageRoomResponseDto.builder()
                .messageRoomId(messageRoom.getMessageRoomId())
                .createAt(messageRoom.getCreateAt())
                .lastMessageAt(messageRoom.getLastMessageAt())
                .messages(messageRoom.getMessages().stream()
                        .map(MessageResponseDto::of)
                        .collect(Collectors.toList()))
                .build();
    }
}