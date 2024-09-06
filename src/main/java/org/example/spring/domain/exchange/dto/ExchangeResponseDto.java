package org.example.spring.domain.exchange.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.spring.constants.SalesStatus;

import java.sql.Timestamp;

@Getter
@Builder
public class ExchangeResponseDto {
    private final Long id;
    private final Long memberId;
    private final String title;
    private final int price;
    private final int regularPrice;
    private final String content;
    private final int viewCount;
    private SalesStatus status;
    private Timestamp createdAt;
}
