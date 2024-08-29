package org.example.spring.domain.message.messageDto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class MessageRoomRequestDto {
    @NotNull
    private Timestamp lastMessageAt;
}
