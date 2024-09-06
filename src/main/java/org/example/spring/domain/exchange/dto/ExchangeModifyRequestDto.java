package org.example.spring.domain.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.spring.constants.SalesStatus;

@Getter
@Builder
@AllArgsConstructor
public class ExchangeModifyRequestDto {
    private final String title;
    private final int price;
    private final String content;
    private final SalesStatus status;
}