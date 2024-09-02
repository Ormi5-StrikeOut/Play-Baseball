package org.example.spring.domain.exchange.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExchangeModifyRequestDto {
    private final String title;
    private final int price;
    private final int regularPrice;
    private final String content;
}
