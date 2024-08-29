package org.example.spring.domain.message.messageDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MessageRequestDto {
    @NotNull
    private Long memberId;

    @NotNull
    private Long messageRoomId;

    @NotBlank
    private String messageContent;
}
