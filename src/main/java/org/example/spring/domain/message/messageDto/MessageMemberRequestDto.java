package org.example.spring.domain.message.messageDto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MessageMemberRequestDto {
    @NotNull
    private Long messageMemberId;

    @NotNull
    private Long memberId;

    @NotNull
    private Long messageRoomId;
}
