package org.example.spring.domain.exchange.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class ExchangeRequestDto {
    private final Long memberId;
    private String title;
    private int price;
    private int regularPrice;
    private String content;
}
