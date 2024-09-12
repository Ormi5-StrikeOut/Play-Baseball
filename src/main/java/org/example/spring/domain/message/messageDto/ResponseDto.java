package org.example.spring.domain.message.messageDto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseDto {

    private Object data;

    public static ResponseDto of(Object data) {
        return ResponseDto.builder()
                .data(data)
                .build();
    }
}