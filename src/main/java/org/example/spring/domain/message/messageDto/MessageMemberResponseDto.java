package org.example.spring.domain.message.messageDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.spring.domain.message.Message;
import org.example.spring.domain.message.MessageMember;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MessageMemberResponseDto {
    @NotNull
    private Long messageMemberId;

    @NotNull
    private Long memberId;

    @NotNull
    private Long messageRoomId;

    public static MessageMemberResponseDto of(MessageMember messageMember) {
        return MessageMemberResponseDto.builder()
                .messageMemberId(messageMember.getMessageMemberId())
                .memberId(messageMember.getMember().getId())
                .messageRoomId(messageMember.getMessageRoom().getMessageRoomId())
                .build();
    }
}
